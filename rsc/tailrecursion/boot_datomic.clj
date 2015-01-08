(ns tailrecursion.boot-datomic
  (:require 
    [boot.core :as core]
    [boot.pod  :as pod] ))

(def ^:private deps
  '[[com.datomic/datomic-transactor-pro "0.9.5078" :scope "provided" :exclusions [org.slf4j/slf4j-nop]]] )

(def ^:private default-opts
 {:protocol               "dev"
  :host                   "localhost"
  :port                   "4334"
  :memory-index-max       "256m"   
  :memory-index-threshold "32m"
  :object-cache-max       "128m" })

(core/deftask datomic

  "Start the datomic transactor.

  The settings are described in http://docs.datomic.com/system-properties.html.

  Memory settings default to the recommended settings for 1G of RAM appropriate for a laptop.
  Reference http://docs.datomic.com/capacity.html."

  [l license-key KEY              str "Required license key."
   r protocol PROTOCOL            str "Storage protocol (dev)."
   u host HOST                    str "Connection host (localhost)."
   p port PORT                    str "Connection port (4334)."
   t memory-index-threshold BYTES str "Threshold at which to start indexing (32m)."
   m memory-index-max BYTES       str "Maximum size of the memory index (256m)."
   c object-cache-max BYTES       str "Size of the object cache (128m)." ]

   (let [pod   (-> (core/get-env) (update-in [:dependencies] into deps) pod/make-pod future) 
         props (merge default-opts *opts*) ]
      (core/with-pre-wrap fileset
        (pod/with-call-in @pod
          (datomic.transactor/run ~props "datomic boot task options") )
        fileset )))
