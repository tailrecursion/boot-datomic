(set-env!
  :resource-paths #{"rsc"}
  :target-path    "tgt"
  :dependencies   '[[org.clojure/clojure                "1.6.0"     :scope "provided"]
                    [boot/core                          "2.0.0-rc5" :scope "provided"]
                    [com.datomic/datomic-pro            "0.9.5078"  :scope "runtime" :exclusions [org.slf4j/slf4j-nop]] 
                    [com.datomic/datomic-transactor-pro "0.9.5078"  :scope "runtime" :exclusions [org.slf4j/slf4j-nop]]
                    [ch.qos.logback/logback-classic     "1.1.2"     :scope "runtime"] ]
  :repositories   #(into % [["datomic"   {:url      "https://my.datomic.com/repo"
                                          :username (System/getenv "DATOMIC_REPO_USERNAME")
                                          :password (System/getenv "DATOMIC_REPO_PASSWORD") }]]))

(require
  '[tailrecursion.boot-datomic :refer [datomic initialize]] )

(load-data-readers!)

(deftask run
  []
  (comp (wait) (speak) (datomic :license-key (System/getenv "DATOMIC_LICENSE_KEY")) ))

#_(deftask test 
  []
  (binding [*data-readers* (merge *data-readers* datomic-data-readers)]
    (comp (wait) (speak) (initialize) (datomic :license-key (System/getenv "DATOMIC_LICENSE_KEY")) )))

(deftask build
  "Build the jar distribution and install it to the local maven repo."
  []
  (comp (speak) (pom) (jar) (install)) )

(def +version+ "0.1.0-SNAPSHOT")


; (def partition 
;   [{:db/id                 #db/id[:db.part/db]
;     :db/ident              :boot-datomic/test
;     :db.install/_partition :db.part/db }])

; (def schema
;   [{:db/id                 #db/id[:db.part/db]
;     :db/ident              :person/first-name
;     :db/valueType          :db.type/string
;     :db/cardinality        :db.cardinality/one
;     :db/fulltext           true
;     :db/doc                "First name of the person."
;     :db.install/_attribute :db.part/db }
;    {:db/id                 #db/id[:db.part/db]
;     :db/ident              :person/last-name
;     :db/valueType          :db.type/string
;     :db/cardinality        :db.cardinality/one
;     :db/fulltext           true
;     :db/doc                "Last name of the person."
;     :db.install/_attribute :db.part/db }])

; (def data 
;   [{:db/id              #db/id[:boot-datomic/test]
;     :person/first-name  "John"
;     :person/last-name   "Doe" }
;    {:db/id              #db/id[:boot-datomic/test]
;     :person/first-name  "Jane"
;     :person/last-name   "Doe" }])

(task-options!
 initialize {:url          "datomic:dev://localhost:4334/test"
             :transactions '[partition schema data] }
 pom  {:project     'tailrecursion/boot-datomic
       :version     +version+
       :description "Boot tasks to manage the datomic database."
       :url         "https://github.com/tailrecursion/boot-datomic"
       :scm         {:url "https://github.com/tailrecursion/boot-datomic"}
       :license     {:name "Eclipse Public License"
                     :url  "http://www.eclipse.org/legal/epl-v10.html" }})