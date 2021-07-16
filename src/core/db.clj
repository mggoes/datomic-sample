(ns core.db
  (:use [clojure pprint])
  (:require [datomic.api :as d]))

(def db-uri "datomic:dev://localhost:4334/ecommerce?password=datomic")

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
;Transact
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

(defn adiciona-produtos!
  ([conn produtos]
   (d/transact conn produtos))
  ([conn produtos ip]
   (let [db-add-ip [:db/add "datomic.tx" :tx-data/ip ip]]
     ;Adiciona informacao na transacao
     (d/transact conn (conj produtos db-add-ip)))))

(defn adiciona-categorias!
  [conn categorias]
  (d/transact conn categorias))

;===================================================
;Consultas
;A linguagem de query do Datomic eh uma extensao do Datalog
(defn todos-os-produtos
  [db]
  (d/q '[:find ?entidade
         :where [?entidade :produto/nome]] db))

(def todos-os-produtos-por-slug-fixo-q
  '[:find ?entidade
    :where [?entidade :produto/slug "/computador_novo"]])

(defn todos-os-produtos-por-slug-fixo
  [db]
  (d/q todos-os-produtos-por-slug-fixo-q db))

(defn todos-os-produtos-por-slug
  [db slug]
  (d/q '[:find ?entidade
         ;Para passar valores para a query utilizamos :in
         ;$ eh uma nomenclatura padrao que indica o parametro db
         ;Quando a query recebe mais de 1 parametro eh necessario especifica o db no :in
         :in $ ?slug-a-ser-buscado
         :where [?entidade :produto/slug ?slug-a-ser-buscado]]
       db slug))

(defn todos-os-slugs
  [db]
  (d/q '[:find ?slug
         ;Caso a variavel nao seja utilizada, podemos usar o underscore (_)
         :where [_ :produto/slug ?slug]] db))

(defn todos-os-produtos-por-preco
  [db]
  (d/q '[:find ?nome ?preco
         ;Consulta com relacionamentos
         ;O where eh executado sequencialmente
         :where [?produto :produto/preco ?preco]
         ;A variavel ?produto foi atribuida no where anterior
         ;Agora ele tentara encontrar os nomes dos produtos encontrados anteriormente
         [?produto :produto/nome ?nome]] db))

(defn todos-os-produtos-por-preco-com-keys
  [db]
  (d/q '[:find ?nome ?preco
         ;Keys define como sera o resultado da consulta
         ;Nesse caso, sera retornado um mapa de produtos com as chaves nome e preco
         :keys produto/nome produto/preco
         :where [?produto :produto/preco ?preco]
         [?produto :produto/nome ?nome]] db))

(defn todos-os-produtos-pull
  [db]
  ;Pull permite especificar os atributos que serao extraidos de um campo
  (d/q '[:find (pull ?entidade [:produto/nome :produto/preco :produto/slug])
         :where [?entidade :produto/nome]] db))

(defn todos-os-produtos-pull-generico
  [db]
  (d/q '[:find (pull ?entidade [*])
         :where [?entidade :produto/nome]] db))

(defn todos-os-produtos-por-preco-minimo
  [db preco-minimo]
  (d/q '[:find ?nome ?preco
         :keys produto/nome produto/preco
         :in $ ?minimo
         :where [?produto :produto/preco ?preco]
         ;Consulta com predicados
         [(> ?preco ?minimo)]
         [?produto :produto/nome ?nome]]
       db preco-minimo))

(defn todos-os-produtos-por-palavra-chave
  [db palavra-chave]
  (d/q '[:find (pull ?produto [*])
         :in $ ?palavra
         :where [?produto :produto/palavra-chave ?palavra]]
       db palavra-chave))

;Recuperando um item pelo id gerado pelo banco
;Por padrao a funcao pull utiliza o identificador do banco
(defn um-produto-por-db-id
  [db db-id]
  (d/pull db '[*] db-id))

;Recuperando um item pelo identificador definido no schema
(defn um-produto
  [db produto-id]
  (d/pull db '[*] [:produto/id produto-id]))

(defn todos-as-categorias
  [db]
  (d/q '[:find (pull ?categoria [*])
         :where [?categoria :categoria/id]] db))

(defn todos-os-nomes-de-produtos-e-categorias
  [db]
  (d/q '[:find ?nome-do-produto ?nome-da-categoria
         :keys produto categoria
         :where [?produto :produto/nome ?nome-do-produto]
         [?produto :produto/categoria ?categoria]
         [?categoria :categoria/nome ?nome-da-categoria]]
       db))

;Fazendo forward no pull
(defn todos-os-produtos-da-categoria
  [db nome-da-categoria]
  ;forward
  (d/q '[:find (pull ?produto [:produto/nome :produto/slug {:produto/categoria [:categoria/nome]}])
         :in $ ?nome
         :where [?categoria :categoria/nome ?nome]
         [?produto :produto/categoria ?categoria]]
       db nome-da-categoria))

;Fazendo backward no pull
(defn todos-os-produtos-da-categoria-2
  [db nome-da-categoria]
  ;backward navigation utiliza underscore (_)
  (d/q '[:find (pull ?categoria [:categoria/nome {:produto/_categoria [:produto/nome :produto/slug]}])
         :in $ ?nome
         :where [?categoria :categoria/nome ?nome]]
       db nome-da-categoria))

;Agrecacoes
(defn resumo-dos-produtos
  [db]
  (d/q '[:find (min ?preco) (max ?preco) (count ?preco)
         :keys minimo maximo total
         ;with realiza o papel do group by
         :with ?produto
         :where [?produto :produto/preco ?preco]] db))

(defn resumo-dos-produtos-por-categoria
  [db]
  (d/q '[:find ?nome (min ?preco) (max ?preco) (count ?preco)
         :keys categoria minimo maximo total
         ;with realiza o papel do group by
         :with ?produto
         :where [?produto :produto/preco ?preco]
         [?produto :produto/categoria ?categoria]
         [?categoria :categoria/nome ?nome]] db))

(defn todos-os-produtos-mais-caros
  [db]
  (let [preco-mais-alto (ffirst (d/q '[:find (max ?preco)
                                       :where [_ :produto/preco ?preco]] db))]
    (d/q '[:find (pull ?produto [*])
           :in $ ?mais-alto
           :where [?produto :produto/preco ?mais-alto]]
         db preco-mais-alto)))

;Nested queries
(defn todos-os-produtos-mais-caros-2
  [db]
  (d/q '[:find (pull ?produto [*])
         :where [(q '[:find (max ?preco)
                      :where [_ :produto/preco ?preco]]
                    ;Dentro do contexto da query o db eh o parametro $
                    ;Depois da nested query definimos o retorno, nesse caso sera a variavel ?preco
                    $) [[?preco]]]
         [?produto :produto/preco ?preco]] db))

;Recuperando informacao da transacao
(defn todos-os-produtos-do-ip
  [db ip]
  (d/q '[:find ?ip-buscado (pull ?produto [:produto/nome])
         :in $ ?ip-buscado
         :where [?transacao :tx-data/ip ?ip-buscado]
         [?produto :produto/id _ ?transacao]]
       db ip))