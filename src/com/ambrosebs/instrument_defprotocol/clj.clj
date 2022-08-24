(ns com.ambrosebs.instrument-defprotocol.clj
  "Instrumentation of clojure.core/defprotocol (Clojure implementation).
  Instrumentation is not threadsafe (do not install methods or otherwise modify
  the protocol during instrumentation), but usage is as threadsafe as protocols.
  
  Compatible with Clojure 1.8+.")

(let [^java.util.Map instrumentation-state (java.util.Collections/synchronizedMap (java.util.WeakHashMap.))]
  (defn- get-instrumentation-state [method-var]
    (locking instrumentation-state
      (or (.get instrumentation-state method-var)
          (let [atm (atom {})]
            (.put instrumentation-state method-var atm)
            atm)))))

(defn sync-method-impl-cache!
  "Propagate method cache from outer-mth (the wrapper) to inner-mth (the wrapped fn).
  
  Explanation: all functions in Clojure have special support for protocol methods
  via the __methodImplCache field: https://github.com/clojure/clojure/search?q=methodimplcache&type=.
  This mutable field is used inside each protocol method's implementation via (fn this [..] (.__methodImplCache this))
  and also mutated from the 'outside' via (set! .__methodImplCache protocol-method).
  Since we wrap protocol methods, we need to preserve these two features (settable from outside, readable from inside)."
  [^clojure.lang.AFunction outer-mth
   ^clojure.lang.AFunction inner-mth]
  ;; cache propagation will usually only happen once per method. we can usually avoid the expensive synchronization.
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
        ^clojure.lang.AFunction outer-mth (instrument-method inner-mth sync-method-cache!)
        ;; populate outer cache so we can use outer-mth as the protocol method without needing
        ;; to call -reset-methods.
        _ (set! (.__methodImplCache outer-mth)
                (.__methodImplCache inner-mth))]
    {:inner-mth inner-mth
     :outer-mth outer-mth
     :original-method-builder (get-method-builder pvar method-var)
     :method-builder (fn [cache]
                       (set! (.__methodImplCache outer-mth) cache)
                       (sync-method-cache! outer-mth inner-mth)
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
  "Given a protocol Var pvar, its method Var method-var and instrument-method,
  instrument the protocol method. Returns a map of unstrument-info that can be
  passed to `unstrument-protocol-method` to undo instrumentation."
  [id ;QualifiedKeyword
   pvar ;:- Var
   method-var ;:- (Var InnerMth)
   instrument-method #_:- #_(s/=>* OuterMth
                                   [InnerMth
                                    (named (=> Any OuterMth InnerMth)
                                           'sync-method-cache!)])]
  (let [atm (get-instrumentation-state method-var)
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
