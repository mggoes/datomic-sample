(ns queries.a3-entities-ref
  (:use [clojure pprint])
  (:require [datomic.api :as d]
            [core.db :as db]
            [core.model :as m]))

(def conn (db/abre-conexao!))
(db/cria-schema! conn)

(def eletronicos (m/nova-categoria "Eletronicos"))
(def esporte (m/nova-categoria "Esporte"))

;(pprint @(d/transact conn [eletronicos esporte]))
(pprint @(db/adiciona-categorias! conn [eletronicos esporte]))
(pprint (db/todos-as-categorias (d/db conn)))

(def computador (m/novo-produto (m/uuid) "Computador Novo" "/computador_novo" 2500.10M))
(def celular (m/novo-produto (m/uuid) "Celular Novo" "/celular" 888888.10M))
(def calculadora {:produto/nome "Calculadora com 4 operações"})
(def celular-barato (m/novo-produto "Celular Barato" "/celular_barato" 0.1M))
(def bola-futebol (m/novo-produto "Bola de futebol" "/bola_futebol" 90M))

;(pprint @(d/transact conn [computador celular calculadora celular-barato bola-futebol]))
(pprint @(db/adiciona-produtos! conn [computador celular calculadora celular-barato bola-futebol]))

(pprint (db/todos-os-produtos-pull-generico (d/db conn)))

;Relacionando entidades com lookup-ref
;(d/transact conn [[:db/add
;                   ;lookup-ref
;                   [:produto/id (:produto/id computador)]
;                   :produto/categoria
;                   [:categoria/id (:categoria/id eletronicos)]]])
;(d/transact conn [[:db/add
;                   [:produto/id (:produto/id celular)]
;                   :produto/categoria
;                   [:categoria/id (:categoria/id eletronicos)]]])
;(d/transact conn [[:db/add
;                   [:produto/id (:produto/id celular-barato)]
;                   :produto/categoria
;                   [:categoria/id (:categoria/id eletronicos)]]])
;(d/transact conn [[:db/add
;                   [:produto/id (:produto/id bola-futebol)]
;                   :produto/categoria
;                   [:categoria/id (:categoria/id esporte)]]])
;(pprint (db/todos-os-produtos-pull-generico (d/db conn)))

(db/atribui-categorias! conn [computador celular celular-barato] eletronicos)
(pprint (db/todos-os-produtos-pull-generico (d/db conn)))

;(db/apaga-banco)