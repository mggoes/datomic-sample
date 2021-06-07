(defproject datomic-sample "0.1.0-SNAPSHOT"
  :description "Datomic sample"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [com.datomic/datomic-pro "1.0.6269"]
                 [prismatic/schema "1.1.12"]]
  :repl-options {:init-ns core.init})
