(ns tailrecursion.boot-datomic.impl
  (:require 
  	[datomic.transactor :as t] ))

(defn datomic [options]
  @(t/run options "datomic boot task options") )