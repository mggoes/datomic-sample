(ns generators-schemas-indexes.db.venda
  (:use [clojure pprint])
  (:require [datomic.api :as d]
            [generators-schemas-indexes.model :as model]
            [generators-schemas-indexes.db.entidade :as db.entidade]))

(defn adiciona!
  [conn produto-id quantidade]
  (let [id (model/uuid)]
    (d/transact conn [{:db/id            "venda"
                       :venda/produto    [:produto/id produto-id]
                       :venda/quantidade quantidade
                       :venda/id         id
                       :venda/situacao   "nova"}])
    id))

(defn instante-da-venda
  [db venda-id]
  (d/q '[:find ?instante .
         :in $ ?id
         :where [_ :venda/id ?id ?tx true]
         [?tx :db/txInstant ?instante]]
    db venda-id))

(defn custo [db venda-id]
  (let [instante (instante-da-venda db venda-id)]
    (d/q '[:find (sum ?preco-por-produto) .
           :in $ ?id
           :where [?venda :venda/id ?id]
           [?venda :venda/quantidade ?quantidade]
           [?venda :venda/produto ?produto]
           [?produto :produto/preco ?preco]
           [(* ?preco ?quantidade) ?preco-por-produto]]
      ;Dessa forma, a consulta sera executada com base nos valores daquele momento que estavam no banco
      ;as-of permite filtrar o banco em um momento especifico do tempo
      (d/as-of db instante) venda-id)))

(defn todas-nao-canceladas
  [db]
  (d/q '[:find ?id
         :where [?venda :venda/id ?id]
         [?venda :venda/situacao ?situacao]
         [(not= ?situacao "cancelada")]]
    db))

(defn todas-inclusive-canceladas-com-historico
  [db]
  (d/q '[:find ?id
         :where [?venda :venda/id ?id _ true]]
    ;history retorna uma base contendo o historico de alteracoes dos datoms, inclusive remocoes
    (d/history db)))

(defn todas-inclusive-canceladas
  [db]
  (d/q '[:find ?id
         :where [?venda :venda/id ?id]]
    db))

(defn todas-canceladas-com-historico
  [db]
  (d/q '[:find ?id
         :where [?venda :venda/id ?id _ false]]
    (d/history db)))

(defn todas-canceladas
  [db]
  (d/q '[:find ?id
         :where [?venda :venda/id ?id]
         [?venda :venda/situacao "cancelada"]]
    db))

(defn altera-situacao!
  [conn venda-id situacao]
  (d/transact conn [{:venda/id       venda-id
                     :venda/situacao situacao}]))

(defn historico
  [db venda-id]
  (->> (d/q '[:find ?instante ?situacao
              :in $ ?id
              :where [?venda :venda/id ?id]
              [?venda :venda/situacao ?situacao ?tx true]
              [?tx :db/txInstant ?instante]]
         (d/history db) venda-id)
    (sort-by first)))

(defn cancela!
  [conn venda-id]
  (altera-situacao! conn venda-id "cancelada"))

(defn historico-geral [db instante-desde]
  (->> (d/q '[:find ?instante ?situacao ?id
              :in $ $filtrado
              :where [$ ?venda :venda/id ?id]
              ;Eh possivel especificar um parametro inicial opcional que indica em qual snapshot a consulta sera feita
              [$filtrado ?venda :venda/situacao ?situacao ?tx true]
              [$filtrado ?tx :db/txInstant ?instante]]
         ;Contrario ao as-of, since recupera um snapshot do banco com eventos a partir de um determinado instante
         db (d/since db instante-desde))
    (sort-by first)))