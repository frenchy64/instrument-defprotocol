(ns com.ambrosebs.instrument-defprotocol.clj-test
  (:require [clojure.test :refer [deftest is]]
            [com.ambrosebs.instrument-defprotocol.clj :as sut]))

(def unstrument-cache (atom {}))

(defn monitor-protocol-method [pname pmethod monitor]
  (sut/instrument-protocol-method
    ::monitor-protocol-method
    pname pmethod
    (fn [inner-mth sync-method-cache!]
      (fn outer-mth [& args]
        (sync-method-cache! outer-mth inner-mth)
        (let [ret (apply inner-mth args)]
          (swap! monitor conj {:args args :ret ret})
          ret)))))

(defn unmonitor-protocol-method [pname pmethod]
  (sut/unstrument-protocol-method
    ::monitor-protocol-method
    pname pmethod
    (fn [inner-mth sync-method-cache!]
      (fn outer-mth [& args]
        (sync-method-cache! outer-mth inner-mth)
        (apply inner-mth args)))))

(deftest sync-method-impl-cache!-test
  (let [outer (fn [])]
    (is (sync-method-impl-cache!
          (fn )))))
