(ns tailrecursion.boot-datomic
  (:require 
    [boot.core :as core]
    [boot.pod  :as pod] ))

(def ^:private deps
  '[[com.datomic/datomic-transactor-pro "0.9.5078" :scope "provided" :exclusions [org.slf4j/slf4j-nop]]] )

(core/deftask datomic

  "Start the datomic transactor.

  The settings are described in http://docs.datomic.com/system-properties.html.

  Memory settings default to the recommended settings for 1G of RAM appropriate for a laptop.
  Reference http://docs.datomic.com/capacity.html."

  [l license-key KEY              str "Required license key."
   r protocol PROTOCOL            str "Storage protocol (dev)."
   u host HOST                    str "Connection host (localhost)."
   p port PORT                    int "Connection port (4334)."
   t memory-index-threshold BYTES str "Threshold at which to start indexing (32m)."
   m memory-index-max BYTES       str "Maximum size of the memory index (256m)."
   c object-cache-max BYTES       str "Size of the object cache (128m)." ]

   (let [p (-> (core/get-env) (update-in [:dependencies] into deps) pod/make-pod future)]
      (core/with-pre-wrap fileset
        (pod/with-call-in @p
          (tailrecursion.boot_datomic.impl/datomic ~*opts*) )
        fileset )))
