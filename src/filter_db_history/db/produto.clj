(ns filter-db-history.db.produto
  (:use [clojure pprint])
  (:require [datomic.api :as d]
            [filter-db-history.model :as model]
            [filter-db-history.db.entidade :as db.entidade]
            [schema.core :as s]
            [clojure.set :as cset])
  (:import (java.util UUID)))

(s/defn adiciona-ou-altera!
  ([conn produtos :- [model/Produto]]
   (d/transact conn produtos))
  ([conn produtos :- [model/Produto] ip]
   (let [db-add-ip [:db/add "datomic.tx" :tx-data/ip ip]]
     (d/transact conn (conj produtos db-add-ip)))))

(s/defn todos :- [model/Produto]
  [db]
  (db.entidade/datomic-para-entidade (d/q '[:find [(pull ?produto [* {:produto/categoria [*]}]) ...]
                                            :where [?produto :produto/nome]] db)))

(s/defn um :- (s/maybe model/Produto)
  [db produto-id :- UUID]
  (let [resultado (d/pull db '[* {:produto/categoria [*]}] [:produto/id produto-id])
        produto (db.entidade/datomic-para-entidade resultado)]
    (if (:produto/id produto)
      produto
      nil)))

(s/defn um! :- model/Produto
  [db produto-id :- UUID]
  (let [produto (um db produto-id)]
    (when (nil? produto)
      (throw (ex-info "Produto nao encontrado" {:type :errors/not-found :id produto-id})))
    produto))

(def regras
  '[[(estoque ?produto ?estoque)
     [?produto :produto/estoque ?estoque]]
    [(estoque ?produto ?estoque)
     [?produto :produto/digital true]
     [(ground 100) ?estoque]]
    [(pode-vender? ?produto)
     (estoque ?produto ?estoque)
     [(> ?estoque 0)]]
    [(produto-na-categoria ?produto ?nome-da-categoria)
     [?categoria :categoria/nome ?nome-da-categoria]
     [?produto :produto/categoria ?categoria]]])

(s/defn todos-os-vendaveis :- [model/Produto]
  [db]
  (db.entidade/datomic-para-entidade (d/q '[:find [(pull ?produto [* {:produto/categoria [*]}]) ...]
                                            :in $ %
                                            :where (pode-vender? ?produto)] db regras)))

(s/defn um-vendavel :- (s/maybe model/Produto)
  [db produto-id :- UUID]
  (let [query '[:find (pull ?produto [* {:produto/categoria [*]}]) .
                :in $ % ?id
                :where [?produto :produto/id ?id]
                (pode-vender? ?produto)]
        resultado (d/q query db regras produto-id)
        produto (db.entidade/datomic-para-entidade resultado)]
    (if (:produto/id produto)
      produto
      nil)))

(s/defn todos-nas-categorias :- [model/Produto]
  [db categorias :- [s/Str]]
  (db.entidade/datomic-para-entidade (d/q '[:find [(pull ?produto [* {:produto/categoria [*]}]) ...]
                                            :in $ % [?nome-da-categoria ...]
                                            :where (produto-na-categoria ?produto ?nome-da-categoria)]
                                       db regras categorias)))

(s/defn todos-nas-categorias-e-digital :- [model/Produto]
  [db categorias :- [s/Str] digital? :- s/Bool]
  (db.entidade/datomic-para-entidade (d/q '[:find [(pull ?produto [* {:produto/categoria [*]}]) ...]
                                            :in $ % [?nome-da-categoria ...] ?eh-digital?
                                            :where (produto-na-categoria ?produto ?nome-da-categoria)
                                            [?produto :produto/digital ?eh-digital?]]
                                       db regras categorias digital?)))

(s/defn atualiza-preco!
  [conn produto-id :- UUID preco-antigo :- BigDecimal preco-novo :- BigDecimal]
  (d/transact conn [[:db/cas [:produto/id produto-id] :produto/preco preco-antigo preco-novo]]))

(s/defn atualiza!
  [conn antigo :- model/Produto a-atualizar :- model/Produto]
  (let [produto-id (:produto/id antigo)
        atributos (cset/intersection (set (keys antigo)) (set (keys a-atualizar)))
        atributos (disj atributos :produto/id)
        txs (map (fn [atributo] [:db/cas [:produto/id produto-id] atributo (get antigo atributo) (get a-atualizar atributo)]) atributos)]
    (d/transact conn txs)))

(defn total
  [db]
  (d/q '[:find [(count ?produto)]
         :where [?produto :produto/nome]
         ] db))

(s/defn remove!
  [conn produto-id :- UUID]
  (d/transact conn [[:db/retractEntity [:produto/id produto-id]]]))

(def incrementa-visualizacao
  #db/fn {:lang   :clojure
          :params [db produto-id]
          :code   (let [visualizacoes (d/q '[:find ?visualizacoes .
                                             :in $ ?id
                                             :where [?produto :produto/id ?id]
                                             [?produto :produto/visualizacoes ?visualizacoes]]
                                        db produto-id)
                        atual (or visualizacoes 0)
                        total-novo (inc atual)]
                    [{:produto/id            produto-id
                      :produto/visualizacoes total-novo}])})

(defn instala-fn-incremento!
  [conn]
  (pprint @(d/transact conn [{:db/doc   "Incrementa o atributo :produto/visualizacoes de uma entidade"
                              :db/ident :incrementa-visualizacao
                              :db/fn    incrementa-visualizacao}])))

(defn historico-de-precos
  [db produto-id]
  (->> (d/q '[:find ?instante ?preco
              :in $ ?id
              :where [?produto :produto/id ?id]
              [?produto :produto/preco ?preco ?tx true]
              [?tx :db/txInstant ?instante]]
         (d/history db) produto-id)
    (sort-by first)))