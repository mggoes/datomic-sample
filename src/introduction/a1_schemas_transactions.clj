(ns introduction.a1-schemas-transactions
  (:use [clojure pprint])
  (:require [datomic.api :as d]
            [core.db :as db]
            [core.model :as m]))

(db/apaga-banco!)
(def conn (db/abre-conexao!))
(db/cria-schema! conn)

;===================================================
;Salvando um item
;No Datomic a unidade basica eh um datom, que eh um fato que ocorreu de forma atomica e que possui informacoes do que aconteceu, alem dos dados alterados/inseridos/removidos
(let [computador (m/novo-produto "Computador Novo" "/computador_novo" 2500.10M)]
  (d/transact conn [computador]))

;===================================================
;Recuperando o banco antes de executar a linha
;Criando uma conexao de leitura
(def db (d/db conn))

;===================================================
;Recuperando um produto do banco
(d/q '[:find ?entidade                                      ;Campos que devem ser retornados
       :where [?entidade :produto/nome]] db)                ;Consulta que sera realizada

(let [celular (m/novo-produto "Celular Novo" "/celular" 888888.10M)]
  (d/transact conn [celular]))

;Recuperando um novo snapshot do banco
(def db (d/db conn))

(d/q '[:find ?entidade
       :where [?entidade :produto/nome]] db)