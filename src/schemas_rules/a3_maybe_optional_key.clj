(ns schemas-rules.a3_maybe_optional_key
  (:use [clojure pprint])
  (:require [datomic.api :as d]
            [schemas-rules.db :as db]
            [schemas-rules.model :as model]
            [schema.core :as s])
  (:import (clojure.lang ExceptionInfo)))

(s/set-fn-validation! true)
(db/apaga-banco!)
(def conn (db/abre-conexao!))
(db/cria-schema! conn)

(db/cria-dados-de-exemplo conn)

(pprint (db/todos-as-categorias (d/db conn)))
(def produtos (db/todos-os-produtos (d/db conn)))
(def primeiro-produto (first produtos))
(pprint primeiro-produto)

(pprint (db/um-produto (d/db conn) (:produto/id primeiro-produto)))
(pprint (db/um-produto (d/db conn) (model/uuid)))

(pprint (db/um-produto! (d/db conn) (:produto/id primeiro-produto)))
(try
  (pprint (db/um-produto! (d/db conn) (model/uuid)))
  (catch ExceptionInfo e
    (.printStackTrace e)
    (pprint (ex-data e))))