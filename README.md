# boot datomic (wip)
run the datomic transactor in a pod. backup and restore the db using boot tasks.

deployment tasks forthcoming. sometime. eventually.

[](dependency)
```clojure
[tailrecursion/boot-datomic "0.1.3"] ;; latest release
```
[](/dependency)

## configuration

install the transactor libary and its dependent libraries into your local maven repo.  ideally, someone at cognitect would notice this and decide it would be a great idea to distribute the transactor alongside the peer in the datomic repo. :)

```bash
boot install-jars -f datomic-pro-0.9.5561.50/datomic-transactor-pro-0.9.5703.jar -f datomic-pro-0.9.5703/lib
```

set the DATOMIC LICENSE KEY environment variable.
```bash
export DATOMIC_LICENSE_KEY=<license-key>
```

add boot-datomic and your preferred logging framework to the environment.

## application
for the sake of expediency, here's an example of a `build.boot` file using this task:
```clojure
(set-env!
  :resource-paths #{"src"}
  :source-paths   #{"ini" "rsc"}
  :dependencies   '[[com.datomic/datomic-pro         "0.9.5703" :exclusions [org.slf4j/slf4j-nop org.slf4j/slf4j-log4j12]]
                    [tailrecursion/boot-datomic      "0.1.2-SNAPSHOT"]
                    [ch.qos.logback/logback-classic  "1.2.3"] ]
  :repositories  [["clojars"       "https://clojars.org/repo/"]
                  ["maven-central" "https://repo1.maven.org/maven2/"]
                  ["datomic"       {:url      "https://my.datomic.com/repo"
                                    :username (System/getenv "DATOMIC_REPO_USERNAME")
                                    :password (System/getenv "DATOMIC_REPO_PASSWORD") }]
                  ["companyname"   {:url      "s3p://companyname.repository/snapshot" ;; transactor jar
                                    :username (System/getenv "COMPANY_AWS_ACCESS_KEY")
                                    :password (System/getenv "COMPANY_AWS_SECRET_KEY") }]])

(require
  '[tailrecursion.boot-datomic :refer [datomic backup restore]] )

(def datomic-data-readers
  {'base64 datomic.codec/base-64-literal
   'db/id  datomic.db/id-literal
   'db/fn  datomic.function/construct })

(def backup-uris
  {:production  "bak/production"
   :staging     "bak/staging"
   :development "bak/development" })

(def datomic-uris
  {:production  "datomic:ddb://us-east-1/foo/bar"
   :staging     "datomic:ddb://us-east-1/foo/bar"
   :development "datomic:dev://localhost:4334/bar" })

(defn backup-uri [environment]
  (.toString (.toURI (io/file (backup-uris environment)))) )

(defn datomic-uri [environment]
  (let [acc-key  (System/getenv "COMPANY_AWS_ACCESS_KEY")
        sec-key  (System/getenv "COMPANY_AWS_SECRET_KEY")
        rmt-env? (or (= environment :production) (= environment :staging))
        base-uri (datomic-uris environment) ]
    (assert acc-key "Missing environment variable COMPANY_AWS_ACCESS_KEY.")
    (assert sec-key "Missing environment variable COMPANY_AWS_SECRET_KEY.")
    (info "Connecting datomic to %s %s environment...\n" (if rmt-env? "remote" "local") (name environment))
    (str base-uri (if rmt-env? (str "?aws_access_key_id=" acc-key "&aws_secret_key=" sec-key))) ))

(deftask run
  []
  (comp (wait) (speak) (datomic :license-key (System/getenv "DATOMIC_LICENSE_KEY")) ))

(deftask backup
  [e environment ENV  kw "The application environment to backup."]
  (assert environment "Missing required environment argument.")
  (db/backup :from-db-uri (datomic-uri environment) :to-backup-uri (backup-uri environment)) )

(deftask restore
  [e environment ENV  kw "The application environment to restore."]
  (assert environment "Missing required environment argument.")
  (db/restore :from-backup-uri (backup-uri environment) :to-db-uri (datomic-uri environment)) )

(deftask develop
  [e environment ENV kw "The backed up environment to restore the local dev db from."]
  (let [init* #(db/restore :from-backup-uri %1 :to-db-uri %2)
        init  #(if % (init* (backup-uri %) (datomic-uri :development)) identity) ]
    (comp (wait) (speak) (datomic) (init environment)) ))
```

## license

copyright (c) jumblerg. all rights reserved.

distributed with clojure under the eclipse public license
