(ns schemas-rules.schemas-schemas
  (:use [clojure pprint])
  (:require [datomic.api :as d]
            [schemas-rules.db :as db]
            [schemas-rules.model :as m]
            [schema.core :as s]))

(s/set-fn-validation! true)
(db/apaga-banco!)
(def conn (db/abre-conexao!))
(db/cria-schema! conn)

(db/cria-dados-de-exemplo conn)

(pprint (db/todos-as-categorias (d/db conn)))
(pprint (db/todos-os-produtos (d/db conn)))

(defn testa-esquema
  []
  (def eletronicos (m/nova-categoria "Eletronicos"))
  (pprint (s/validate m/Categoria eletronicos))

  (def computador (m/novo-produto (m/uuid) "Computador Novo" "/computador_novo" 2500.10M))
  (pprint (s/validate m/Produto (assoc computador :produto/categoria eletronicos))))

(testa-esquema)
