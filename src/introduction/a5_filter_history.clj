(ns introduction.a5-filter-history
  (:use [clojure pprint])
  (:require [datomic.api :as d]
            [core.db :as db]
            [core.model :as m]))

(db/apaga-banco!)
(def conn (db/abre-conexao!))
(db/cria-schema! conn)

(let [computador (m/novo-produto "Computador Novo" "/computador_novo" 2500.10M)
      celular (m/novo-produto "Celular Novo" "/celular" 888888.10M)
      resultado @(d/transact conn [computador celular])]
  (pprint resultado))

;Snapshot
(def passado (d/db conn))

(let [calculadora {:produto/nome "Calculadora com 4 operações"}
      celular-barato (m/novo-produto "Celular Barato" "/celular_barato" 0.1M)
      resultado @(d/transact conn [calculadora celular-barato])]
  (pprint resultado))

;Recuperando um snapshot
(pprint (count (db/todos-os-produtos-pull (d/db conn))))
(pprint (count (db/todos-os-produtos-pull passado)))

;Recuperando um banco filtrado, ou seja, um snapshot do passado
;as-of permite especificar um momento no passado para recuperar um snapshot do banco
(pprint (count (db/todos-os-produtos-pull (d/as-of (d/db conn) #inst "2021-05-28T21:26:42.370"))))