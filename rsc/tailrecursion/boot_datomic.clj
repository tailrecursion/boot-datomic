(ns tailrecursion.boot-datomic
  (:require
    [boot.pod  :as pod] 
    [boot.core :refer :all] ))

(def ^:private deps
  '[[com.datomic/datomic-transactor-pro "0.9.5078" :exclusions [org.slf4j/slf4j-nop]]
    [ch.qos.logback/logback-classic     "1.1.2"] ])

(def ^:private default-opts
 {:protocol               "dev"
  :host                   "localhost"
  :port                   "4334"
  :memory-index-max       "256m"   
  :memory-index-threshold "32m"
  :object-cache-max       "128m" })

(deftask datomic

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

   (let [pod  (future (-> (get-env) (update-in [:dependencies] into deps) pod/make-pod))
         opts (into default-opts *opts*) ]
      (with-pre-wrap fileset
        (pod/with-call-in @pod
          (tailrecursion.boot-datomic.impl/datomic ~opts) )
        fileset )))