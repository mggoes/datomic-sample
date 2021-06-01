(ns introduction.a6-optimizations
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
  (pprint @(d/transact conn [computador celular calculadora celular-barato])))

(db/todos-os-produtos-por-preco-minimo (d/db conn) 1000)
(db/todos-os-produtos-por-preco-minimo (d/db conn) 5000)

;Adicionando informacao no campo com cardinalidade many
(d/transact conn [[:db/add 17592186045418 :produto/palavra-chave "desktop"]
                  [:db/add 17592186045418 :produto/palavra-chave "computador"]])
(pprint (db/todos-os-produtos-pull-generico (d/db conn)))

;Removendo informacao no campo com cardinalidade many
(d/transact conn [[:db/retract 17592186045418 :produto/palavra-chave "computador"]])
(pprint (db/todos-os-produtos-pull-generico (d/db conn)))

;Adicionando informacao no campo com cardinalidade many
(d/transact conn [[:db/add 17592186045418 :produto/palavra-chave "monitor preto e branco"]])
(pprint (db/todos-os-produtos-pull-generico (d/db conn)))

(d/transact conn [[:db/add 17592186045419 :produto/palavra-chave "celular"]
                  [:db/add 17592186045421 :produto/palavra-chave "celular"]])
(pprint (db/todos-os-produtos-pull-generico (d/db conn)))

;Busca por palavra-chave
(pprint (db/todos-os-produtos-por-palavra-chave (d/db conn) "celular"))
(pprint (db/todos-os-produtos-por-palavra-chave (d/db conn) "monitor preto e branco"))

;(db/apaga-banco)