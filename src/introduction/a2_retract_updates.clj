(ns introduction.a2-retract-updates
  (:use [clojure pprint])
  (:require [datomic.api :as d]
            [core.db :as db]
            [core.model :as m]))

(db/apaga-banco!)
(def conn (db/abre-conexao!))
(db/cria-schema! conn)

;===================================================
;Datomic suporta inserir entidades sem todos os atributos
(let [calculadora {:produto/nome "Calculadora com 4 operações"}]
  (d/transact conn [calculadora]))

;Nil nao e um valor valido para o campo :produto/slug
;(let [radio-relogio {:produto/nome "Rádio com relógio" :produto/slug nil}]
;(d/transact conn [radio-relogio]))

;===================================================
;Atualizando um valor
;Para esperar o resultado de future basta dereferencia-la utilizando @
(let [celular-barato (m/novo-produto "Celular Barato" "/celular_barato" 888888.10M)
      resultado @(d/transact conn [celular-barato])
      id-entidade (-> resultado :tempids vals first)]
  (pprint resultado)
  (pprint @(d/transact conn [[:db/add id-entidade :produto/preco 0.1M]]))
  ;Removendo um valor
  (pprint @(d/transact conn [[:db/retract id-entidade :produto/slug "/celular_barato"]])))