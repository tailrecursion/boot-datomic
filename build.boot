(set-env!
  :resource-paths #{"rsc"}
  :source-paths   #{"src"}
  :target-path    "tgt"
  :dependencies   '[[org.clojure/clojure            "1.7.0"  :scope "provided"]
                    [boot/core                      "2.1.2"  :scope "provided"]
                    [ch.qos.logback/logback-classic "1.1.2"  :scope "provided"]
                    [adzerk/bootlaces               "0.1.11" :scope "test"] ]
  :repositories    [["clojars"       "https://clojars.org/repo/"]
                    ["maven-central" "https://repo1.maven.org/maven2/"]
                    ["datomic"       {:url      "https://my.datomic.com/repo"
                                      :username (System/getenv "DATOMIC_REPO_USERNAME")
                                      :password (System/getenv "DATOMIC_REPO_PASSWORD") }]])

(require
  '[adzerk.bootlaces           :refer :all]
  '[tailrecursion.boot-datomic :refer :all] )

(def +version+ "0.1.0-SNAPSHOT")

(bootlaces! +version+)

(deftask run
  []
  (comp (wait) (speak) (datomic :license-key (System/getenv "DATOMIC_LICENSE_KEY")) ))

(task-options!
 pom  {:project     'tailrecursion/boot-datomic
       :version     +version+
       :description "Boot task to run the datomic transactor."
       :url         "https://github.com/tailrecursion/boot-datomic"
       :scm         {:url "https://github.com/tailrecursion/boot-datomic"}
       :license     {"EPL" "http://www.eclipse.org/legal/epl-v10.html"} })
