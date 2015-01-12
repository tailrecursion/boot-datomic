(set-env!
  :resource-paths #{"rsc"}
  :source-paths   #{"src"}
  :target-path    "tgt"
  :dependencies   '[[org.clojure/clojure            "1.6.0"     :scope "provided"]
                    [boot/core                      "2.0.0-rc6" :scope "provided"]
                    [ch.qos.logback/logback-classic "1.1.2"     :scope "provided"] ]
  :repositories   #(into % [["datomic"   {:url      "https://my.datomic.com/repo"
                                          :username (System/getenv "DATOMIC_REPO_USERNAME")
                                          :password (System/getenv "DATOMIC_REPO_PASSWORD") }]]))

(require
  '[tailrecursion.boot-datomic :refer :all] )

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
       :description "Boot task to run the datomic transactor."
       :url         "https://github.com/tailrecursion/boot-datomic"
       :scm         {:url "https://github.com/tailrecursion/boot-datomic"}
       :license     {"EPL" "http://www.eclipse.org/legal/epl-v10.html"} })