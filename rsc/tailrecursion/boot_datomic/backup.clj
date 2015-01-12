(ns tailrecursion.boot-datomic.backup
  (:require 
    [datomic.backup-cli :as b] ))

(defn list-backups [options]
  (println (b/list-backups options)) )