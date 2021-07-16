(ns generators-schemas-indexes.db.config
  (:use [clojure pprint])
  (:require [datomic.api :as d]
            [generators-schemas-indexes.model :as model]
            [generators-schemas-indexes.db.categoria :as db.categoria]
            [generators-schemas-indexes.db.produto :as db.produto]
            [generators-schemas-indexes.db.venda :as db.venda]))

(def db-uri "datomic:dev://localhost:4334/ecommerce?password=datomic")

(defn abre-conexao! []
  (d/create-database db-uri)
  (d/connect db-uri))

(defn apaga-banco! []
  (d/delete-database db-uri))

(def schema [{:db/ident       :produto/nome
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
              :db/index       true
              :db/doc         "Preco de um produto"}
             {:db/ident       :produto/palavra-chave
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/many
              :db/doc         "Palavras chaves"}
             {:db/ident       :produto/id
              :db/valueType   :db.type/uuid
              :db/cardinality :db.cardinality/one
              :db/unique      :db.unique/identity
              :db/doc         "ID do produto"}
             {:db/ident       :produto/categoria
              :db/valueType   :db.type/ref
              :db/cardinality :db.cardinality/one
              :db/doc         "Categoria do produto"}
             {:db/ident       :produto/estoque
              :db/valueType   :db.type/long
              :db/cardinality :db.cardinality/one
              :db/doc         "Estoque do produto"}
             {:db/ident       :produto/digital
              :db/valueType   :db.type/boolean
              :db/cardinality :db.cardinality/one
              :db/doc         "Indica se o produto eh digital"}
             {:db/ident       :produto/variacao
              :db/valueType   :db.type/ref
              :db/cardinality :db.cardinality/many
              :db/isComponent true
              :db/doc         "Variacao do produto"}
             {:db/ident       :produto/visualizacoes
              :db/valueType   :db.type/long
              :db/cardinality :db.cardinality/one
              :db/noHistory   true
              :db/doc         "Quantidades de visualizacoes"}

             {:db/ident       :variacao/id
              :db/valueType   :db.type/uuid
              :db/cardinality :db.cardinality/one
              :db/unique      :db.unique/identity}
             {:db/ident       :variacao/nome
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one}
             {:db/ident       :variacao/preco
              :db/valueType   :db.type/bigdec
              :db/cardinality :db.cardinality/one}

             {:db/ident       :categoria/nome
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "Nome da categoria"}
             {:db/ident       :categoria/id
              :db/valueType   :db.type/uuid
              :db/cardinality :db.cardinality/one
              :db/unique      :db.unique/identity
              :db/doc         "ID da categoria"}

             {:db/ident       :tx-data/ip
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "IP de origem da operacao"}

             {:db/ident       :venda/id
              :db/valueType   :db.type/uuid
              :db/cardinality :db.cardinality/one
              :db/unique      :db.unique/identity}
             {:db/ident       :venda/produto
              :db/valueType   :db.type/ref
              :db/cardinality :db.cardinality/one}
             {:db/ident       :venda/quantidade
              :db/valueType   :db.type/long
              :db/cardinality :db.cardinality/one}
             {:db/ident       :venda/situacao
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one}])

(defn cria-schema!
  [conn]
  (d/transact conn schema))

(defn cria-dados-de-exemplo!
  [conn]
  (def eletronicos (model/nova-categoria "Eletronicos"))
  (def esporte (model/nova-categoria "Esporte"))
  (db.categoria/adiciona! conn [eletronicos esporte])
  (def computador (model/novo-produto (model/uuid) "Computador Novo" "/computador_novo" 2500.10M 10))
  (def celular (model/novo-produto (model/uuid) "Celular Novo" "/celular" 888888.10M))
  (def celular-barato (model/novo-produto "Celular Barato" "/celular_barato" 0.1M))
  (def xadres (model/novo-produto (model/uuid) "Tabuleiro de xadrez" "/tabuleiro_xadrez" 30M 5))
  (def jogo (assoc (model/novo-produto (model/uuid) "Jogo online" "/jogo_online" 20M) :produto/digital true))
  (db.produto/adiciona-ou-altera! conn [computador celular celular-barato xadres jogo] "127.0.0.1")

  (db.categoria/atribui! conn [computador celular celular-barato jogo] eletronicos)
  (db.categoria/atribui! conn [xadres] esporte)

  (db.venda/adiciona! conn (:produto/id computador) 3)
  (db.venda/adiciona! conn (:produto/id computador) 4)
  (db.venda/adiciona! conn (:produto/id computador) 8))