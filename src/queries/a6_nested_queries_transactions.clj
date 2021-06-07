(ns queries.a6-nested-queries-transactions
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
(def celular-barato (m/novo-produto "Celular Barato" "/celular_barato" 30M))
(def bola-futebol (m/novo-produto "Bola de futebol" "/bola_futebol" 90M))

(pprint @(db/adiciona-produtos! conn [computador celular calculadora celular-barato bola-futebol] "127.0.0.1"))
(pprint (db/todos-os-produtos-pull-generico (d/db conn)))

(db/atribui-categorias! conn [computador celular celular-barato] eletronicos)
(db/atribui-categorias! conn [bola-futebol] esporte)

;Adicionando produto com uma categoria
(pprint @(db/adiciona-produtos! conn [{:produto/nome      "Camiseta"
                                       :produto/slug      "/camiseta"
                                       :produto/preco     30M
                                       :produto/id        (m/uuid)
                                       :produto/categoria {:categoria/nome "Roupas"
                                                           :categoria/id   (m/uuid)}}]))
(pprint (db/todos-os-nomes-de-produtos-e-categorias (d/db conn)))

;Adicionando produto com uma categoria ja existente
(pprint @(db/adiciona-produtos! conn [{:produto/nome      "Bola de tenis"
                                       :produto/slug      "/bola_tenis"
                                       :produto/preco     45M
                                       :produto/id        (m/uuid)
                                       ;Lookup ref
                                       :produto/categoria [:categoria/id (:categoria/id esporte)]}]))
(pprint (db/todos-os-nomes-de-produtos-e-categorias (d/db conn)))
(pprint (db/todos-os-produtos-pull (d/db conn)))

(pprint (db/todos-os-produtos-mais-caros (d/db conn)))
(pprint (db/todos-os-produtos-mais-caros-2 (d/db conn)))

;Buscando por informacoes na transacao
(pprint (db/todos-os-produtos-do-ip (d/db conn) "127.0.0.1"))