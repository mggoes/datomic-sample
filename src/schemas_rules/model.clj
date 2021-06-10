(ns schemas-rules.model
  (:import (java.util UUID))
  (:require [schema.core :as s]))

(def Categoria
  {:categoria/id   UUID
   :categoria/nome s/Str})

(def Produto
  {(s/optional-key :produto/nome)          s/Str
   (s/optional-key :produto/slug)          s/Str
   (s/optional-key :produto/preco)         BigDecimal
   :produto/id                             UUID
   (s/optional-key :produto/palavra-chave) [s/Str]
   (s/optional-key :produto/categoria)     Categoria})

(defn uuid [] (UUID/randomUUID))

(defn novo-produto
  ([nome slug preco]
   (novo-produto (uuid) nome slug preco))
  ([uuid nome slug preco]
   {:produto/id    uuid
    :produto/nome  nome
    :produto/slug  slug
    :produto/preco preco}))

(defn nova-categoria
  ([nome]
   (nova-categoria (uuid) nome))
  ([uuid nome]
   {:categoria/id   uuid
    :categoria/nome nome}))