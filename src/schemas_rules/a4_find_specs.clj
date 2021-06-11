(ns schemas-rules.a4_find_specs
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

(pprint (db/todos-os-produtos (d/db conn)))
(pprint (db/todos-os-produtos-com-estoque (d/db conn)))

(def produtos (db/todos-os-produtos (d/db conn)))
(def primeiro (first produtos))
(pprint primeiro)
(pprint (db/um-produto-com-estoque (d/db conn) (:produto/id primeiro)))