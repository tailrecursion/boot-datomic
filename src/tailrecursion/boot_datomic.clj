(ns tailrecursion.boot-datomic
  {:boot/export-tasks true}
  (:require
    [clojure.java.io :as io]
    [boot.pod        :as pod]
    [boot.util       :as util]
    [boot.core       :as core :refer [deftask]]))

(deftask install-jars
  "Install the jars with maven pom files"
  [f files PATH #{str} "Paths to jar files and directories."]
  (let [{dirs true files false} (group-by #(.isDirectory %) (map io/file files))
        files                   (concat (mapcat file-seq dirs) files)]
    (core/with-pass-thru [fs]
      (doseq [jarpath (filter #(re-find #".jar$" %) (map #(.getPath %) files))]
        (util/info "Installing %s...\n" jarpath)
        (try
          (pod/with-call-worker
            (boot.aether/install ~(core/get-env) ~jarpath nil))
          (catch Exception e
            (util/info "No pom file in jar %s. Installation skipped.\n" jarpath)))))))

(def ^:private deps
  "Datomic transactor to load if none is provided via the project."
  (->> '[[com.amazonaws/aws-java-sdk-dynamodb "1.11.382"]
         [com.datomic/datomic-transactor-pro  "0.9.5703" :exclusions [org.slf4j/slf4j-nop jline-win/jline-win bsh/bsh]]]
       (remove pod/dependency-loaded?)
       (cons '[org.clojure/clojure "1.9.0"])
       (delay)))

(defn make-pod []
  (-> (core/get-env)
      (update :dependencies into (vec (seq @deps)))
      (pod/make-pod)
      (future)))

(deftask create-dynamodb-table
  "Create a new DynamoDB table for use by Datomic.

  For more information reference http://docs.datomic.com/storage.html."

  [n table-name NAME      str "Required name of the table"
   g region REGION        kw  "Required AWS region"
   r read-capacity WRITES int "Optional read capacity (100)"
   w write-capacity READS int "Optional write capacity (50)"]

   (let [pod  (make-pod)
         opts (into {:read-capacity 100 :write-capacity 50} *opts*)]
      (core/with-pre-wrap fileset
        (pod/with-call-in @pod
          (datomic.provisioning.aws/create-system-command ~opts))
        fileset)))

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
      (core/with-pre-wrap fileset
        (pod/with-call-in @pod
          (datomic.backup-cli/backup ~opts))
        fileset)))

(deftask list-backups
  "List the approximate points in time (t) of available backups made of the
  database.

  The source URI may refer to the local filesystem or an S3 bucket as shown
  below.

      file:/full/path/to/backup-directory
      s3://bucket/prefix

  For more information reference http://docs.datomic.com/backup.html."

  [s backup-uri URI str "Required backup source"]

   (let [pod  (make-pod)]
      (core/with-pre-wrap fileset
        (-> @pod
          (pod/with-call-in (datomic.backup-cli/list-backups ~*opts*))
          (println))
        fileset)))

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
      (core/with-pre-wrap fileset
        (pod/with-call-in @pod
          (datomic.backup-cli/restore ~opts))
        fileset)))

(def ^:private transactor-defaults
 {:protocol               "dev"
  :host                   "localhost"
  :port                   "4334"
  :memory-index-max       "256m"
  :memory-index-threshold "32m"
  :object-cache-max       "128m"})

(deftask datomic
  "Start the transactor.

  The settings are described in http://docs.datomic.com/system-properties.html.

  Memory settings default to the recommended settings for 1G of RAM appropriate for a laptop.

  For more information reference http://docs.datomic.com/capacity.html."

  [k license-key KEY              str  "License key (required)."
   r protocol PROTOCOL            str  "Storage protocol (dev)."
   u host HOST                    str  "Connection host (localhost)."
   p port PORT                    str  "Connection port (4334)."
   l log-dir PATH                 str  "The directory the logger will write to (/log)."
   t memory-index-threshold BYTES str  "Threshold at which to start indexing (32m)."
   m memory-index-max BYTES       str  "Maximum size of the memory index (256m)."
   c object-cache-max BYTES       str  "Size of the object cache (128m)."
   V volatile                     bool "Delete the data on each call (false)."]

   (let [dir      (core/cache-dir! ::data)
         pod      (make-pod)
         opts*    (assoc (dissoc *opts* :volatile) :data-dir (.getPath dir))
         opts     (into transactor-defaults opts*)
         message #(util/info "%s the Datomic transactor on port %s...\n" % (:port opts))
         empty   #(when volatile (core/empty-dir! %)) ]
      (core/cleanup
        (message  "\nStopping")
        (pod/with-call-in @pod
          (datomic.api/shutdown true))
        (empty dir) )
      (core/with-pre-wrap fileset
        (message  "Starting")
        (empty dir)
        (pod/with-call-in @pod
          (datomic.transactor/run ~opts "datomic boot task options"))
        (Thread/sleep 1000)
        fileset)))


