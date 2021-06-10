(ns schemas-rules.db
  (:use [clojure pprint])
  (:require [datomic.api :as d]
            [schemas-rules.model :as m]
            [schema.core :as s]
            [clojure.walk :as walk])
  (:import (java.util UUID)))

(def db-uri "datomic:dev://localhost:4334/ecommerce")

;===================================================
;Criando uma base de dados
;Conectando na base criada
(defn abre-conexao! []
  (d/create-database db-uri)
  (d/connect db-uri))

;===================================================
;Removendo a base de dados
(defn apaga-banco! []
  (d/delete-database db-uri))

;===================================================
;Definindo um esquema
(def schema [;========================================
             ;Produto
             {:db/ident       :produto/nome
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "O nome de um produto"}
             {:db/ident       :produto/slug
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "O caminho para acessar esse produto via HTTP"}
             {:db/ident       :produto/preco
              :db/valueType   :db.type/bigdec
              :db/cardinality :db.cardinality/one
              :db/doc         "Preco de um produto"}
             {:db/ident       :produto/palavra-chave
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/many
              :db/doc         "Palavras chaves"}
             ;Definindo um identificador unico
             {:db/ident       :produto/id
              :db/valueType   :db.type/uuid
              :db/cardinality :db.cardinality/one
              :db/unique      :db.unique/identity
              :db/doc         "ID do produto"}
             {:db/ident       :produto/categoria
              ;Referenciando um tipo
              :db/valueType   :db.type/ref
              :db/cardinality :db.cardinality/one
              :db/doc         "Categoria do produto"}
             ;========================================
             ;Categoria
             {:db/ident       :categoria/nome
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "Nome da categoria"}
             {:db/ident       :categoria/id
              :db/valueType   :db.type/uuid
              :db/cardinality :db.cardinality/one
              :db/unique      :db.unique/identity
              :db/doc         "ID da categoria"}
             ;========================================
             ;Transacoes
             {:db/ident       :tx-data/ip
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "IP de origem da operacao"}])

(defn cria-schema!
  [conn]
  (d/transact conn schema))

;===================================================
(defn db-adds
  [produtos categoria]
  (reduce (fn [db-adds produto] (conj db-adds [:db/add
                                               [:produto/id (:produto/id produto)]
                                               :produto/categoria
                                               [:categoria/id (:categoria/id categoria)]]))
    []
    produtos))

(defn atribui-categorias!
  [conn produtos categoria]
  (let [a-transactionar (db-adds produtos categoria)]
    (d/transact conn a-transactionar)))

(s/defn adiciona-ou-altera-produtos!
  ([conn produtos :- [m/Produto]]
   (d/transact conn produtos))
  ([conn produtos :- [m/Produto] ip]
   (let [db-add-ip [:db/add "datomic.tx" :tx-data/ip ip]]
     ;Adiciona informacao na transacao
     (d/transact conn (conj produtos db-add-ip)))))

(s/defn adiciona-categorias!
  [conn categorias :- [m/Categoria]]
  (d/transact conn categorias))

;===================================================
;Pre-Walk
;Navega no mapa realizando uma operacao especifica
(defn dissoc-db-id
  [entidade]
  (if (map? entidade)
    (dissoc entidade :db/id)
    entidade))

(defn datomic-para-entidade
  [entidades]
  (walk/prewalk dissoc-db-id entidades))

(s/defn todos-as-categorias :- [m/Categoria]
  [db]
  ;Colocando o pull entre colchetes [] faz com que o Datomic retorne o resultado sem encapsular nos colchetes
  ;Quando utilizado colchetes o Datomic retorna apenas 1 elemento, para retorna todos eh preciso utilizar reticencias (...)
  (datomic-para-entidade (d/q '[:find [(pull ?categoria [*]) ...]
                                :where [?categoria :categoria/id]] db)))

(s/defn todos-os-produtos :- [m/Produto]
  [db]
  (datomic-para-entidade (d/q '[:find [(pull ?produto [* {:produto/categoria [*]}]) ...]
                                :where [?produto :produto/nome]] db)))

;===================================================
(defn cria-dados-de-exemplo
  [conn]
  (def eletronicos (m/nova-categoria "Eletronicos"))
  (def esporte (m/nova-categoria "Esporte"))
  (adiciona-categorias! conn [eletronicos esporte])

  (def computador (m/novo-produto (m/uuid) "Computador Novo" "/computador_novo" 2500.10M))
  (def celular (m/novo-produto (m/uuid) "Celular Novo" "/celular" 888888.10M))
  ;(def calculadora {:produto/nome "Calculadora com 4 operações"})
  (def celular-barato (m/novo-produto "Celular Barato" "/celular_barato" 0.1M))
  (def xadres (m/novo-produto "Tabuleiro de xadres" "/tabuleiro_xadres" 30M))
  (adiciona-ou-altera-produtos! conn [computador celular celular-barato xadres] "127.0.0.1")

  (atribui-categorias! conn [computador celular celular-barato] eletronicos)
  (atribui-categorias! conn [xadres] esporte))

;===================================================
;Maybe
;Permite especificar um tipo que deve satisfazer o schema ou ser nil
(s/defn um-produto :- (s/maybe m/Produto)
  [db produto-id :- UUID]
  (let [resultado (d/pull db '[* {:produto/categoria [*]}] [:produto/id produto-id])
        produto (datomic-para-entidade resultado)]
    (if (:produto/id produto)
      produto
      nil)))

(s/defn um-produto! :- m/Produto
  [db produto-id :- UUID]
  (let [produto (um-produto db produto-id)]
    (when (nil? produto)
      (throw (ex-info "Produto nao encontrado" {:type :errors/not-found :id produto-id})))
    produto))