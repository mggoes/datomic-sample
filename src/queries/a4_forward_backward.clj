(ns queries.a4-forward-backward
  (:use [clojure pprint])
  (:require [datomic.api :as d]
            [core.db :as db]
            [core.model :as m]))

(db/apaga-banco!)
(def conn (db/abre-conexao!))
(db/cria-schema! conn)

(def eletronicos (m/nova-categoria "Eletronicos"))
(def esporte (m/nova-categoria "Esporte"))

(pprint @(db/adiciona-categorias! conn [eletronicos esporte]))
(pprint (db/todos-as-categorias (d/db conn)))

(def computador (m/novo-produto (m/uuid) "Computador Novo" "/computador_novo" 2500.10M))
(def celular (m/novo-produto (m/uuid) "Celular Novo" "/celular" 888888.10M))
(def calculadora {:produto/nome "Calculadora com 4 operações"})
(def celular-barato (m/novo-produto "Celular Barato" "/celular_barato" 0.1M))
(def bola-futebol (m/novo-produto "Bola de futebol" "/bola_futebol" 90M))

(pprint @(db/adiciona-produtos! conn [computador celular calculadora celular-barato bola-futebol]))
(pprint (db/todos-os-produtos-pull-generico (d/db conn)))

(db/atribui-categorias! conn [computador celular celular-barato] eletronicos)
(db/atribui-categorias! conn [bola-futebol] esporte)

(pprint (db/todos-os-produtos-pull-generico (d/db conn)))
(pprint (db/todos-os-nomes-de-produtos-e-categorias (d/db conn)))
(pprint (db/todos-os-produtos-da-categoria (d/db conn) "Eletronicos"))
(pprint (db/todos-os-produtos-da-categoria (d/db conn) "Esporte"))

(pprint (db/todos-os-produtos-da-categoria-2 (d/db conn) "Esporte"))
(pprint (db/todos-os-produtos-da-categoria-2 (d/db conn) "Eletronicos"))