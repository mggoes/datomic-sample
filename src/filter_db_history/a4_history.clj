(ns filter-db-history.a4-history
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
(db.venda/adiciona! conn (:produto/id primeiro) 4)
(db.venda/adiciona! conn (:produto/id primeiro) 8)

(pprint @(db.venda/cancela! conn venda1))

(pprint (db.venda/todas-nao-canceladas (d/db conn)))
(pprint (db.venda/todas-inclusive-canceladas (d/db conn)))
(pprint (db.venda/todas-canceladas (d/db conn)))

(pprint @(db.produto/adiciona-ou-altera! conn [{:produto/id    (:produto/id primeiro)
                                                :produto/preco 300M}]))

(pprint @(db.produto/adiciona-ou-altera! conn [{:produto/id    (:produto/id primeiro)
                                                :produto/preco 250M}]))

(pprint @(db.produto/adiciona-ou-altera! conn [{:produto/id    (:produto/id primeiro)
                                                :produto/preco 277M}]))

(pprint @(db.produto/adiciona-ou-altera! conn [{:produto/id    (:produto/id primeiro)
                                                :produto/preco (:produto/preco primeiro)}]))

(pprint (db.produto/historico-de-precos (d/db conn) (:produto/id primeiro)))