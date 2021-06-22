(ns generators-schemas-indexes.db.categoria
  (:use [clojure pprint])
  (:require [datomic.api :as d]
            [generators-schemas-indexes.model :as model]
            [generators-schemas-indexes.db.entidade :as db.entidade]
            [schema.core :as s]))

(defn db-adds
  [produtos categoria]
  (reduce (fn [db-adds produto] (conj db-adds [:db/add
                                               [:produto/id (:produto/id produto)]
                                               :produto/categoria
                                               [:categoria/id (:categoria/id categoria)]]))
    []
    produtos))

(defn atribui!
  [conn produtos categoria]
  (let [a-transactionar (db-adds produtos categoria)]
    (d/transact conn a-transactionar)))

(s/defn adiciona!
  [conn categorias :- [model/Categoria]]
  (d/transact conn categorias))

(s/defn todos :- [model/Categoria]
  [db]
  (db.entidade/datomic-para-entidade (d/q '[:find [(pull ?categoria [*]) ...]
                                            :where [?categoria :categoria/id]] db)))