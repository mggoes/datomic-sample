(ns bindings-transactionfunctions-filters.a2_transaction_functions
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

(pprint @(db/atualiza-preco! conn (:produto/id primeiro) (:produto/preco primeiro) 30M))
(pprint @(db/atualiza-preco! conn (:produto/id primeiro) 30M 35M))
;(pprint @(db/atualiza-preco! conn (:produto/id primeiro) 30M 45M))

(def segundo (second produtos))
(pprint segundo)
(def a-atualizar {:produto/id (:produto/id segundo) :produto/preco 3000M :produto/estoque 8})

(db/atualiza-produto! conn segundo a-atualizar)
