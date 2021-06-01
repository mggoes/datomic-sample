(ns queries.a2-add-update
  (:use [clojure pprint])
  (:require [datomic.api :as d]
            [core.db :as db]
            [core.model :as m]))

(def conn (db/abre-conexao!))
(db/cria-schema! conn)

(def computador (m/novo-produto (m/uuid) "Computador Novo" "/computador_novo" 2500.10M))
(def celular (m/novo-produto (m/uuid) "Celular Novo" "/celular" 888888.10M))
(def calculadora {:produto/nome "Calculadora com 4 operações"})
(def celular-barato (m/novo-produto "Celular Barato" "/celular_barato" 0.1M))

(pprint @(d/transact conn [computador celular calculadora celular-barato]))
(pprint (db/todos-os-produtos-pull-generico (d/db conn)))

(def celular-barato-2 (m/novo-produto (:produto/id celular-barato) "Celular Barato 2" "/celular_barato_2" 0.001M))
(pprint @(d/transact conn [celular-barato-2]))

(pprint (db/todos-os-produtos-pull-generico (d/db conn)))

;(db/apaga-banco)