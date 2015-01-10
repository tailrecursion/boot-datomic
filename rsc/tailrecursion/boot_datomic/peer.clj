(ns tailrecursion.boot_datomic.peer
  (:require 
    [datomic.api :as d] ))

(defn initialize [uri & transactions]
  (d/delete-database uri)
  (d/create-database uri)
  (when-let [conn (d/connect uri)]
    (doseq [data transactions]
      (println (d/transact conn data)) )))