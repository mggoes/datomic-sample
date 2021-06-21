(ns filter-db-history.a2-time-problems
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
(pprint venda1)

(def venda2 (db.venda/adiciona! conn (:produto/id primeiro) 4))
(pprint venda2)

(pprint (db.venda/custo (d/db conn) venda1))
(pprint (db.venda/custo (d/db conn) venda2))

@(db.produto/adiciona-ou-altera! conn [{:produto/id    (:produto/id primeiro)
                                       :produto/preco 300M}])

(pprint (db.venda/custo (d/db conn) venda1))
(pprint (db.venda/custo (d/db conn) venda2))