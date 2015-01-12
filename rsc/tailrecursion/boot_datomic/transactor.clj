(ns tailrecursion.boot-datomic.transactor
  (:require 
    [datomic.transactor :as t] ))

(defn run [options]
  @(t/run options "datomic boot task options") )