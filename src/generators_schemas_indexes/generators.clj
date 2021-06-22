(ns generators-schemas-indexes.generators
  (:use [clojure pprint])
  (:require [clojure.test.check.generators :as gen]
            [schema-generators.generators :as g]
            [generators-schemas-indexes.model :as model]))

(defn double-para-bigdecimal
  [valor]
  (BigDecimal. (str valor)))

(def double-finito (gen/double* {:infinite? false :NaN? false}))
(def bigdecimal (gen/fmap double-para-bigdecimal double-finito))

(def leaf-generators {BigDecimal bigdecimal})