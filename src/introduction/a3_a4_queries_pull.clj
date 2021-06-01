(ns introduction.a3-a4-queries-pull
  (:use [clojure pprint])
  (:require [datomic.api :as d]
            [core.db :as db]
            [core.model :as m]))

(def conn (db/abre-conexao!))
(db/cria-schema! conn)

(let [computador (m/novo-produto "Computador Novo" "/computador_novo" 2500.10M)
      celular (m/novo-produto "Celular Novo" "/celular" 888888.10M)
      calculadora {:produto/nome "Calculadora com 4 operações"}
      celular-barato (m/novo-produto "Celular Barato" "/celular_barato" 0.1M)]
  (d/transact conn [computador celular calculadora celular-barato]))

(pprint (db/todos-os-produtos (d/db conn)))
(pprint (db/todos-os-produtos-por-slug-fixo (d/db conn)))
(pprint (db/todos-os-produtos-por-slug (d/db conn) "/computador_novo"))

(pprint (db/todos-os-slugs (d/db conn)))
(pprint (db/todos-os-produtos-por-preco (d/db conn)))
(pprint (db/todos-os-produtos-por-preco-com-keys (d/db conn)))

(pprint (db/todos-os-produtos-pull (d/db conn)))
(pprint (db/todos-os-produtos-pull-generico (d/db conn)))

;(db/apaga-banco!)