(ns generators-schemas-indexes.a1-leaf-generators
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

(pprint (db.produto/todos (d/db conn)))

(pprint (g/sample 10 model/Categoria))
(pprint (g/sample 10 model/Variacao generators/leaf-generators))


