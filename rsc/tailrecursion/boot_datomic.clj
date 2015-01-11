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

(core/deftask datomic

  "Start the transactor.

  The settings are described in http://docs.datomic.com/system-properties.html.

  Memory settings default to the recommended settings for 1G of RAM appropriate for a laptop.
  Reference http://docs.datomic.com/capacity.html."

  [k license-key KEY              str "License key (required)."
   r protocol PROTOCOL            str "Storage protocol (dev)."
   u host HOST                    str "Connection host (localhost)."
   p port PORT                    str "Connection port (4334)."
   l log-dir PATH                 str "The directory the logger will write to (/log)."
   t memory-index-threshold BYTES str "Threshold at which to start indexing (32m)."
   m memory-index-max BYTES       str "Maximum size of the memory index (256m)."
   c object-cache-max BYTES       str "Size of the object cache (128m)." ]

   (let [opts (merge default-opts *opts*)
         jars '[com.datomic/datomic-transactor-pro ch.qos.logback/logback-classic]
         pod  (-> (core/get-env) 
                  (update-in [:dependencies] (partial filterv (fn [[a]] (some #{a} jars))))
                  pod/make-pod 
                  future ) ]
      (core/with-pre-wrap fileset
        (pod/with-call-in @pod
          (datomic.transactor/run ~opts "datomic boot task options") )
        fileset )))