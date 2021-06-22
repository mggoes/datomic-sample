(ns generators-schemas-indexes.a2-generating-products
  (:use [clojure pprint])
  (:require [datomic.api :as d]
            [generators-schemas-indexes.db.config :as db.config]
            [generators-schemas-indexes.db.produto :as db.produto]
            [generators-schemas-indexes.db.venda :as db.venda]
            [generators-schemas-indexes.model :as model]
            [generators-schemas-indexes.generators :as generators]
            [schema.core :as s]
            [schema-generators.generators :as g]
            [clojure.test.check.generators :as gen]))

(s/set-fn-validation! true)

(db.config/apaga-banco!)
(def conn (db.config/abre-conexao!))
(db.config/cria-schema! conn)
(db.config/cria-dados-de-exemplo! conn)

(defn gera-10000-produtos
  [conn]
  (dotimes [atual 50]
    (def produtos-gerados (g/sample 200 model/Produto generators/leaf-generators))
    (println atual "==>" (count @(db.produto/adiciona-ou-altera! conn produtos-gerados)))))

(time (gera-10000-produtos conn))

(time (dotimes [_ 100] (db.produto/busca-mais-caro (d/db conn))))
(time (dotimes [_ 100] (count (db.produto/busca-mais-caros-que (d/db conn) 50000M))))



