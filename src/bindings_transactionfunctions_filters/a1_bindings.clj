(ns bindings-transactionfunctions-filters.a1-bindings
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

;(pprint (db/todos-os-produtos (d/db conn)))

(pprint (db/todos-os-produtos-nas-categorias (d/db conn) ["Eletronicos" "Alimentacao"]))
;(pprint (db/todos-os-produtos-nas-categorias (d/db conn) ["Eletronicos" "Esporte"]))
;(pprint (db/todos-os-produtos-nas-categorias (d/db conn) ["Esporte"]))
;(pprint (db/todos-os-produtos-nas-categorias (d/db conn) []))
;(pprint (db/todos-os-produtos-nas-categorias (d/db conn) ["Alimentacao"]))

(pprint (db/todos-os-produtos-nas-categorias-e-digital (d/db conn) ["Esporte"] true))
(pprint (db/todos-os-produtos-nas-categorias-e-digital (d/db conn) ["Esporte"] false))
(pprint (db/todos-os-produtos-nas-categorias-e-digital (d/db conn) ["Eletronicos"] true))
