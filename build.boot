(set-env!
  :source-paths   #{"cnf"}
  :resource-paths #{"src"}
  :target-path    "tgt"
  :dependencies   '[[org.clojure/clojure                 "1.9.0"  :scope "provided"]
                    [ch.qos.logback/logback-classic      "1.2.3"  :scope "provided"]
                    [adzerk/bootlaces                    "0.1.13" :scope "test"]]
  :repositories    [["clojars"       "https://clojars.org/repo/"]
                    ["maven-central" "https://repo1.maven.org/maven2/"]
                    ["datomic"       {:url      "https://my.datomic.com/repo"
                                      :username (System/getenv "DATOMIC_REPO_USERNAME")
                                      :password (System/getenv "DATOMIC_REPO_PASSWORD")}]])

(require
  '[adzerk.bootlaces           :refer [bootlaces! build-jar push-release]]
  '[tailrecursion.boot-datomic :refer :all])

(def +version+ "0.1.3")

(bootlaces! +version+)

(deftask run
  []
  (comp (wait) (speak) (datomic)))

(deftask develop []
  (comp (watch) (speak) (build-jar)))

(deftask deploy []
  (comp (build-jar) (push-release)))

(task-options!
  pom     {:project     'tailrecursion/boot-datomic
           :version     +version+
           :description "Boot task to run the datomic transactor."
           :url         "https://github.com/tailrecursion/boot-datomic"
           :scm         {:url "https://github.com/tailrecursion/boot-datomic"}
           :license     {"EPL" "http://www.eclipse.org/legal/epl-v10.html"}}
  datomic {:license-key (System/getenv "DATOMIC_LICENSE_KEY")})
