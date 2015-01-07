(ns tailrecursion.boot_datomic.impl
  (:require 
    [datomic.launcher :as t] ))

(def defaults 
 {:protocol               "dev"
  :host                   "localhost"
  :port                   4334
  :memory-index-max       "256m"   
  :memory-index-threshold "32m"
  :object-cache-max       "128m" })

(defn datomic 
  "Start the datomic transactor."
  [{:keys [protocol host port memory-index-max memory-index-threshold object-cache-max] :as opts}]
  (let [cmp-opts (merge defaults opts)
        str-opts (zipmap (map name (keys cmp-opts)) (map str (vals cmp-opts)))
        props    (doto (java.util.Properties.) (.putAll str-opts)) ]
    (t/run-transactor props nil) ))
