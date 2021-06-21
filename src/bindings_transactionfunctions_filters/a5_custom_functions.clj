(ns bindings-transactionfunctions-filters.a5-custom-functions
  (:use [clojure pprint])
  (:require [datomic.api :as d]
            [bindings-transactionfunctions-filters.db :as db]
            [bindings-transactionfunctions-filters.model :as model]
            [schema.core :as s]))

(s/set-fn-validation! true)
(db/apaga-banco!)
(def conn (db/abre-conexao!))
(db/cria-schema! conn)
(db/cria-dados-de-exemplo conn)

(def produtos (db/todos-os-produtos (d/db conn)))
(def primeiro (first produtos))
(pprint primeiro)

(db/instala-fn-incremento! conn)

(dotimes [_ 10] (db/visualizacao! conn (:produto/id primeiro)))
(pprint (db/um-produto (d/db conn) (:produto/id primeiro)))