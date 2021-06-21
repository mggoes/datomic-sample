(ns schemas-rules.a5-rules
  (:use [clojure pprint])
  (:require [datomic.api :as d]
            [schemas-rules.db :as db]
            [schemas-rules.model :as model]
            [schema.core :as s]))

(s/set-fn-validation! true)
(db/apaga-banco!)
(def conn (db/abre-conexao!))
(db/cria-schema! conn)
(db/cria-dados-de-exemplo conn)

;(pprint (db/todos-os-produtos (d/db conn)))
(pprint (db/todos-os-produtos-vendaveis (d/db conn)))

(def produtos (db/todos-os-produtos (d/db conn)))
(pprint (db/um-produto-vendavel (d/db conn) (:produto/id (first produtos))))