(ns com.ambrosebs.instrument-defprotocol.clj
  "Instrumentation of clojure.core/defprotocol (Clojure implementation).
  Instrumentation is not threadsafe (do not install methods or otherwise modify
  the protocol during instrumentation), but usage is as threadsafe as protocols.
  
  Compatible with Clojure 1.8 through 1.11.")

(let [^java.util.Map instrumentation-state (java.util.Collections/synchronizedMap (java.util.WeakHashMap.))]
  (defn- get-instrumentation-state [method-var]
    (locking instrumentation-state
      (or (.get instrumentation-state method-var)
          (let [atm (atom {})]
            (.put instrumentation-state method-var atm)
            atm)))))

(defn sync!
  "Propagate method cache from outer-mth (the wrapper) to inner-mth
  (the wrapped fn)."
  [^clojure.lang.AFunction outer-mth
   ^clojure.lang.AFunction inner-mth]
  (when-not (identical? (.__methodImplCache outer-mth)
                        (.__methodImplCache inner-mth))
    ;; lock to prevent outdated outer caches from overwriting newer inner caches
    (locking inner-mth
      (set! (.__methodImplCache inner-mth)
            ;; vv WARNING: must be calculated within protected area
            (.__methodImplCache outer-mth)
            ;; ^^ WARNING: must be calculated within protected area
            ))))

(defn get-method-builder
  [pvar method-var]
  {:post [(ifn? %)]}
  (get-in @pvar [:method-builders method-var]))

(defn install-method-builder!
  [pvar method-var method-builder]
  (alter-var-root pvar assoc-in [:method-builders method-var] method-builder))

(defn create-method-builder
  [pvar method-var instrument-method]
  (let [^clojure.lang.AFunction inner-mth @method-var
        ^clojure.lang.AFunction outer-mth (instrument-method inner-mth sync!)
        ;; populate outer cache so we can use outer-mth as the protocol method without needing
        ;; to call -reset-methods.
        _ (set! (.__methodImplCache outer-mth)
                (.__methodImplCache inner-mth))]
    {:inner-mth inner-mth
     :outer-mth outer-mth
     :original-method-builder (get-method-builder pvar method-var)
     :method-builder (fn [cache]
                       (set! (.__methodImplCache outer-mth) cache)
                       (sync! outer-mth inner-mth)
                       ;; preempt future fix for CLJ-1796--have a canonical method
                       ;; representation for the duration of the protocol, matching
                       ;; CLJS semantics.
                       outer-mth)}))

(defn install-method!
  "Directly install method instead of calling -reset-methods."
  [method-var outer-mth]
  (alter-var-root method-var (fn [_] outer-mth)))

(defn disable-protocol-method-inlining!
  "Ensure method-var is never inlined by the compiler."
  [^clojure.lang.Var method-var]
  (alter-meta! method-var assoc :inline (fn [& args]
                                          `((do ~(symbol (-> method-var .ns ns-name name) (-> method-var .sym str)))
                                            ~@args))))

(defn enable-protocol-method-inlining!
  "Revert the effects of disable-protocol-method-inlining!."
  [^clojure.lang.Var method-var]
  (alter-meta! method-var dissoc :inline))

(defn instrument-protocol-method
  "Given a protocol Var pvar, its method method-var and instrument-method,
  instrument the protocol method. Returns a map of unstrument-info that can be
  passed to `unstrument-protocol-method` to undo instrumentation."
  [id ;QualifiedKeyword
   pvar ;:- Var
   method-var ;:- (Var InnerMth)
   instrument-method #_:- #_(s/=>* OuterMth
                                   [InnerMth
                                    (named (=> Any OuterMth InnerMth)
                                           'sync!)])]
  (let [atm (get-instrumentation-state )
        {:keys [outer-mth method-builder] :as unstrument-info} (create-method-builder method-var instrument-method)]
    ;; instrument method builder
    (install-method-builder! pvar method-var method-builder)
    (disable-protocol-method-inlining! method-var)
    ;; instrument the actual method
    (install-method! method-var outer-mth)
    unstrument-info))

(defn unstrument-protocol-method
  "Revert the effects of instrument-protocol-method. Pass the result of instrument-protocol-method
  as `unstrument-info`."
  [id
   pvar
   method-var
   {:keys [inner-mth original-method-builder]
    :as unstrument-info}]
  (install-method-builder! pvar method-var original-method-builder)
  (enable-protocol-method-inlining! method-var)
  (install-method! method-var inner-mth))

;; example
#_
(defn parse-defprotocol-sig [env pname name+sig+doc]
  (let [inner-mth (gensym)
        gen-binder (fn [gs bind]
                     (vec (mapcat #(list %1 :- (-> %2 meta :schema)) gs bind)))
        gen-bind-syms (fn [bind]
                        (mapv (fn [s]
                                (if (symbol? s)
                                  (gensym (str (name s) "__"))
                                  (gensym)))
                              bind))
        outer-mth (gensym (str method-name "__"))
        sync! (gensym)]
    `(-instrument-protocol-method
       (var ~pname)
       (var ~method-name)
       ;; a function that wraps a protocol method in a schema check with a
       ;; cache synchronization point
       (fn [~inner-mth ~sync!]
         (schema.core/fn ~(with-meta outer-mth {})
           :- ~output-schema
           ~@(map (fn [bind]
                    (let [gs (gen-bind-syms bind)]
                      (list (gen-binder gs bind)
                            (list sync! outer-mth inner-mth)
                            (cons inner-mth gs))))
                  binds))))))
