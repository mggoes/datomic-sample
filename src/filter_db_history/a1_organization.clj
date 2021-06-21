(ns filter-db-history.a1-organization
  (:use [clojure pprint])
  (:require [datomic.api :as d]
            [filter-db-history.db.config :as db.config]
            [filter-db-history.db.produto :as db.produto]
            [schema.core :as s]))

(s/set-fn-validation! true)

(db.config/apaga-banco!)
(def conn (db.config/abre-conexao!))
(db.config/cria-schema! conn)
(db.config/cria-dados-de-exemplo! conn)

(pprint (db.produto/todos (d/db conn)))