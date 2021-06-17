(ns bindings-transactionfunctions-filters.a3_components_functions
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

(pprint @(db/adiciona-variacao! conn (:produto/id primeiro) "Computador gamer" 6000M))
(pprint @(db/adiciona-variacao! conn (:produto/id primeiro) "Computador empresarial" 4000M))