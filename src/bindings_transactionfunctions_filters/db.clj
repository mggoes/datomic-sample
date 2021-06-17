(ns bindings-transactionfunctions-filters.db
  (:use [clojure pprint])
  (:require [datomic.api :as d]
            [bindings-transactionfunctions-filters.model :as m]
            [schema.core :as s]
            [clojure.walk :as walk]
            [clojure.set :as cset])
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
              :db/doc         "Variacao do produto"}
             ;========================================
             ;Variacao
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

  (def computador (m/novo-produto (m/uuid) "Computador Novo" "/computador_novo" 2500.10M 10))
  (def celular (m/novo-produto (m/uuid) "Celular Novo" "/celular" 888888.10M))
  ;(def calculadora {:produto/nome "Calculadora com 4 operações"})
  (def celular-barato (m/novo-produto "Celular Barato" "/celular_barato" 0.1M))
  (def xadres (m/novo-produto (m/uuid) "Tabuleiro de xadrez" "/tabuleiro_xadrez" 30M 5))
  (def jogo (assoc (m/novo-produto (m/uuid) "Jogo online" "/jogo_online" 20M) :produto/digital true))
  (adiciona-ou-altera-produtos! conn [computador celular celular-barato xadres jogo] "127.0.0.1")

  (atribui-categorias! conn [computador celular celular-barato jogo] eletronicos)
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

;===================================================
;Rules
(def regras
  '[
    [(estoque ?produto ?estoque)
     [?produto :produto/estoque ?estoque]]

    [(estoque ?produto ?estoque)
     [?produto :produto/digital true]
     ;A funcao ground pode ser utilizada dentro de uma regra para fixar um valor para um simbolo
     [(ground 100) ?estoque]]

    ;Combinando regras
    [(pode-vender? ?produto)
     (estoque ?produto ?estoque)
     [(> ?estoque 0)]]

    [(produto-na-categoria ?produto ?nome-da-categoria)
     [?categoria :categoria/nome ?nome-da-categoria]
     [?produto :produto/categoria ?categoria]]
    ])

(s/defn todos-os-produtos-vendaveis :- [m/Produto]
  [db]
  (datomic-para-entidade (d/q '[:find [(pull ?produto [* {:produto/categoria [*]}]) ...]
                                :in $ %
                                :where (pode-vender? ?produto)] db regras)))

(s/defn um-produto-vendavel :- (s/maybe m/Produto)
  [db produto-id :- UUID]
  ;No find spec um ponto no final faz com que o Datomic retorne apenas 1 elemento (um escalar)
  ;https://docs.datomic.com/on-prem/query/query.html#find-specifications
  (let [query '[:find (pull ?produto [* {:produto/categoria [*]}]) .
                ;Semelhante ao banco, o padrao de nomenclatura para regras eh porcentagem (%)
                :in $ % ?id
                :where [?produto :produto/id ?id]
                ;Utilizando a regra
                (pode-vender? ?produto)]
        resultado (d/q query db regras produto-id)
        produto (datomic-para-entidade resultado)]
    (if (:produto/id produto)
      produto
      nil)))

(s/defn todos-os-produtos-nas-categorias :- [m/Produto]
  [db categorias :- [s/Str]]
  (datomic-para-entidade (d/q '[:find [(pull ?produto [* {:produto/categoria [*]}]) ...]
                                ;Collection binding
                                ;Semelhante a um IN no SQL
                                :in $ % [?nome-da-categoria ...]
                                :where (produto-na-categoria ?produto ?nome-da-categoria)]
                           db regras categorias)))

(s/defn todos-os-produtos-nas-categorias-e-digital :- [m/Produto]
  [db categorias :- [s/Str] digital? :- s/Bool]
  (datomic-para-entidade (d/q '[:find [(pull ?produto [* {:produto/categoria [*]}]) ...]
                                ;Collection binding
                                ;Semelhante a um IN no SQL
                                :in $ % [?nome-da-categoria ...] ?eh-digital?
                                :where (produto-na-categoria ?produto ?nome-da-categoria)
                                [?produto :produto/digital ?eh-digital?]]
                           db regras categorias digital?)))

;===================================================
;Transaction functions
(s/defn atualiza-preco!
  [conn produto-id :- UUID preco-antigo :- BigDecimal preco-novo :- BigDecimal]
  ;:db/cas executa uma funcao na transacao para comparar e definir um valor
  (d/transact conn [[:db/cas [:produto/id produto-id] :produto/preco preco-antigo preco-novo]]))

(s/defn atualiza-produto!
  [conn antigo :- m/Produto a-atualizar :- m/Produto]
  (let [produto-id (:produto/id antigo)
        atributos (cset/intersection (set (keys antigo)) (set (keys a-atualizar)))
        atributos (disj atributos :produto/id)
        txs (map (fn [atributo] [:db/cas [:produto/id produto-id] atributo (get antigo atributo) (get a-atualizar atributo)]) atributos)]
    (d/transact conn txs)))

(s/defn adiciona-variacao!
  [conn produto-id :- UUID variacao :- s/Str preco :- BigDecimal]
  ;Temp ID
  ;Eh possivel definir um ID temporario para utilizar dentro da mesma transacao
  (d/transact conn [{:db/id          "variacao-temporaria"
                     :variacao/nome  variacao
                     :variacao/preco preco
                     :variacao/id    (m/uuid)}
                    {:produto/id       produto-id
                     :produto/variacao "variacao-temporaria"}]))