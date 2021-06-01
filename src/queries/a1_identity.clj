(ns queries.a1-identity
  (:use [clojure pprint])
  (:require [datomic.api :as d]
            [core.db :as db]
            [core.model :as m]))

(def conn (db/abre-conexao!))
(db/cria-schema! conn)

(let [computador (m/novo-produto (m/uuid) "Computador Novo" "/computador_novo" 2500.10M)
      celular (m/novo-produto (m/uuid) "Celular Novo" "/celular" 888888.10M)
      calculadora {:produto/nome "Calculadora com 4 operações"}
      celular-barato (m/novo-produto "Celular Barato" "/celular_barato" 0.1M)]
  (pprint @(d/transact conn [computador celular calculadora celular-barato])))

(pprint (db/todos-os-produtos-pull-generico (d/db conn)))

(pprint (db/um-produto-por-db-id (d/db conn) 17592186045421))

(def produtos (db/todos-os-produtos-pull-generico (d/db conn)))
(def primeiro-produto-db-id (-> produtos
                                first
                                first
                                :db/id))
(println "O id do primeiro produto" primeiro-produto-db-id)
(pprint (db/um-produto-por-db-id (d/db conn) primeiro-produto-db-id))

(def produto-id (-> produtos
                    ffirst
                    :produto/id))
(println "O id do primeiro produto" produto-id)
(pprint (db/um-produto (d/db conn) produto-id))

;(db/apaga-banco)