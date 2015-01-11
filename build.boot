(set-env!
  :resource-paths #{"rsc"}
  :target-path     "tgt"
  :dependencies   '[[org.clojure/clojure                "1.6.0"     :scope "provided"]
                    [boot/core                          "2.0.0-rc5" :scope "provided"]
                    [com.datomic/datomic-transactor-pro "0.9.5078"  :scope "provided" :exclusions [org.slf4j/slf4j-nop]]
                    [ch.qos.logback/logback-classic     "1.1.2"     :scope "runtime"] ]
  :repositories   #(into % [["datomic"   {:url      "https://my.datomic.com/repo"
                                          :username (System/getenv "DATOMIC_REPO_USERNAME")
                                          :password (System/getenv "DATOMIC_REPO_PASSWORD") }]]))

(require
  '[tailrecursion.boot-datomic :refer [datomic]] )

(def +version+ "0.1.0-SNAPSHOT")

(deftask run
  []
  (comp (wait) (speak) (datomic :license-key (System/getenv "DATOMIC_LICENSE_KEY")) ))

(deftask build
  "Build the jar distribution and install it to the local maven repo."
  []
  (comp (speak) (pom) (jar) (install)) )

(task-options!
 pom  {:project     'tailrecursion/boot-datomic
       :version     +version+
       :description "Boot tasks to manage the datomic database."
       :url         "https://github.com/tailrecursion/boot-datomic"
       :scm         {:url "https://github.com/tailrecursion/boot-datomic"}
       :license     {:name "Eclipse Public License"
                     :url  "http://www.eclipse.org/legal/epl-v10.html" }})