(ns generators-schemas-indexes.db.variacao
  (:use [clojure pprint])
  (:require [datomic.api :as d]
            [generators-schemas-indexes.model :as model]
            [schema.core :as s])
  (:import (java.util UUID)))

(s/defn adiciona!
  [conn produto-id :- UUID variacao :- s/Str preco :- BigDecimal]
  (d/transact conn [{:db/id          "variacao-temporaria"
                     :variacao/nome  variacao
                     :variacao/preco preco
                     :variacao/id    (model/uuid)}
                    {:produto/id       produto-id
                     :produto/variacao "variacao-temporaria"}]))