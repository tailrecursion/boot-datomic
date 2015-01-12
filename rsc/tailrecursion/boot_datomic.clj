(ns tailrecursion.boot-datomic
  (:require
    [boot.pod  :as    pod] 
    [boot.core :refer :all] ))

(def ^:private deps
  '[[com.datomic/datomic-transactor-pro "0.9.5078" :exclusions [ch.qos.logback/logback-classic org.slf4j/slf4j-nop]]] )

(defn make-pod []
  (future (-> (get-env) (update-in [:dependencies] into deps) pod/make-pod)))

(deftask backup
  "Backup the database.

  The destination URI may refer to the local filesystem or an S3 bucket as 
  shown below.

      file:/full/path/to/backup-directory
      s3://bucket/prefix

  Encryption is only supported on S3 using AWS' managed server side encryption 
  keys (SSE-S3).

  For more information reference http://docs.datomic.com/backup.html."

  [s from-db-uri URI   str  "Required backup source"
   t to-backup-uri URI str  "Required backup target"
   e encryption        bool "Use AWS SSE-S3 encryption (false)"]

   (let [pod  (make-pod)
         opts (assoc *opts* :encryption (if encryption :sse))]
      (with-pre-wrap fileset
        (pod/with-call-in @pod
          (datomic.backup-cli/backup ~opts) )
        fileset )))

(deftask restore
  "Restore the database.

  The source URI may refer to the local filesystem or an S3 bucket as shown 
  below.

      file:/full/path/to/backup-directory
      s3://bucket/prefix

  This task restores the database to the most recent restoration point (t) by
  default, but can optionally restore the database to another t value.

  For more information reference http://docs.datomic.com/backup.html."

  [s from-backup-uri URI str "Required restore source"
   t to-db-uri URI       str "Required restore target"
   m time T              int "Optional restoration point (most recent)"]

   (let [pod  (make-pod)
         opts (clojure.set/rename-keys *opts* {:time :t})]
      (with-pre-wrap fileset
        (pod/with-call-in @pod
          (datomic.backup-cli/restore ~opts) )
        fileset )))

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

  For more information reference http://docs.datomic.com/capacity.html."

  [k license-key KEY              str "License key (required)."
   r protocol PROTOCOL            str "Storage protocol (dev)."
   u host HOST                    str "Connection host (localhost)."
   p port PORT                    str "Connection port (4334)."
   l log-dir PATH                 str "The directory the logger will write to (/log)."
   t memory-index-threshold BYTES str "Threshold at which to start indexing (32m)."
   m memory-index-max BYTES       str "Maximum size of the memory index (256m)."
   c object-cache-max BYTES       str "Size of the object cache (128m)." ]

   (let [pod  (make-pod)
         opts (into default-opts *opts*) ]
      (with-pre-wrap fileset
        (pod/with-call-in @pod
          (tailrecursion.boot-datomic.impl/datomic ~opts) )
        fileset )))