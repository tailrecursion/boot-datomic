(ns tailrecursion.boot-datomic
  (:require 
    [boot.core :as core]
    [boot.pod  :as pod] ))

(def ^:private default-opts
 {:protocol               "dev"
  :host                   "localhost"
  :port                   "4334"
  :memory-index-max       "256m"   
  :memory-index-threshold "32m"
  :object-cache-max       "128m" })

(defn make-pod-from-env [& artifacts]
  (-> (core/get-env) 
      (update-in [:dependencies] (partial filterv (fn [[a]] (some #{a} artifacts))))
      pod/make-pod 
      future ))

(core/deftask initialize

  "Initialize the database."

  [u uri URI            str  "URI of the transactor."
   t transactions EDN #{edn} "Data to initialize the database with." ]

   (let [pod (make-pod-from-env 'com.datomic/datomic-pro 'ch.qos.logback/logback-classic) ]
      (core/with-post-wrap _
        (pod/with-call-in @pod
          (tailrecursion.boot-datomic.peer/initialize ~*opts*) ))))

(core/deftask datomic

  "Start the transactor.

  The settings are described in http://docs.datomic.com/system-properties.html.

  Memory settings default to the recommended settings for 1G of RAM appropriate for a laptop.
  Reference http://docs.datomic.com/capacity.html."

  [k license-key KEY              str "Required license key."
   r protocol PROTOCOL            str "Storage protocol (dev)."
   u host HOST                    str "Connection host (localhost)."
   p port PORT                    str "Connection port (4334)."
   t memory-index-threshold BYTES str "Threshold at which to start indexing (32m)."
   m memory-index-max BYTES       str "Maximum size of the memory index (256m)."
   c object-cache-max BYTES       str "Size of the object cache (128m)." ]

   (let [pod   (make-pod-from-env 'com.datomic/datomic-transactor-pro 'ch.qos.logback/logback-classic)
         props (merge default-opts *opts*) ]
      (core/with-pre-wrap fileset
        (pod/with-call-in @pod
          (datomic.transactor/run ~props "datomic boot task options") )
        fileset )))
