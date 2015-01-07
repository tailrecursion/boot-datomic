(ns tailrecursion.boot-datomic
  (:require 
    [boot.core                       :as core]
    [boot.pod                        :as pod] ))

(def ^:private deps
  '[[com.datomic/datomic-transactor-pro "0.9.5078" :scope "provided" :exclusions [org.slf4j/slf4j-nop]]] )

(core/deftask datomic

  "Start the datomic transactor."

  [l license-key KEY             str "The datomic license key."
   r protocol NAME               str "The protocol (dev or production)."
   u host URL                    str "The host (localhost)."
   p port PORT                   int "The port (4334)."
   m memory-index-max NAME       str "The memory index maximum (256m)."
   t memory-index-threshold NAME str "The memory index threshold (32m)."
   c object-cache-max NAME       str "The object cache maximum (128m)." ]

   (let [p (-> (core/get-env) (update-in [:dependencies] into deps) pod/make-pod future)]
      (core/with-pre-wrap fileset
        (pod/with-call-in @p
          (tailrecursion.boot_datomic.impl/datomic ~*opts*) )
        fileset )))
