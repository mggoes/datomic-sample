(ns filter-db-history.a5-optimizations-filter-joins
  (:use [clojure pprint])
  (:require [datomic.api :as d]
            [filter-db-history.db.config :as db.config]
            [filter-db-history.db.produto :as db.produto]
            [filter-db-history.db.venda :as db.venda]
            [schema.core :as s]))

(s/set-fn-validation! true)

(db.config/apaga-banco!)
(def conn (db.config/abre-conexao!))
(db.config/cria-schema! conn)
(db.config/cria-dados-de-exemplo! conn)

(def produtos (db.produto/todos (d/db conn)))
(def primeiro (first produtos))
(pprint primeiro)

(def venda1 (db.venda/adiciona! conn (:produto/id primeiro) 3))
(def venda2 (db.venda/adiciona! conn (:produto/id primeiro) 4))
(def venda3 (db.venda/adiciona! conn (:produto/id primeiro) 8))
(pprint venda1)

(pprint @(db.venda/altera-situacao! conn venda1 "preparando"))
(pprint @(db.venda/altera-situacao! conn venda2 "preparando"))
(pprint @(db.venda/altera-situacao! conn venda2 "a caminho"))
(pprint @(db.venda/altera-situacao! conn venda2 "entregue"))

(pprint (db.venda/historico (d/db conn) venda2))

(pprint @(db.venda/cancela! conn venda1))
(pprint (db.venda/historico (d/db conn) venda1))

(pprint (count (db.venda/todas-nao-canceladas (d/db conn))))
(pprint (count (db.venda/todas-inclusive-canceladas (d/db conn))))
(pprint (count (db.venda/todas-canceladas (d/db conn))))

(pprint (db.venda/historico-geral (d/db conn) #inst "2020-06-21T18:21:03.546-00:00"))
(pprint (db.venda/historico-geral (d/db conn) #inst "2021-06-21T18:23:27.776-00:00"))

