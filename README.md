# boot datomic (experimental)
run the datomic transactor embedded in a boot pod

## setup

install the datomic transactor pro jar into your local maven repository.
```bash
mvn install:install-file -DgroupId=com.datomic -DartifactId=datomic-transactor-pro -Dfile=datomic-transactor-pro-0.9.5078.jar -DpomFile=pom.xml
```

configure the BOOT_JVM_OPTIONS and DATOMIC LICENSE KEY environment variables. 
```bash
export BOOT_JVM_OPTIONS="-client -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -Xmx2g -XX:MaxPermSize=128m -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -Xverify:none"
export DATOMIC_LICENSE_KEY=<license-key>
```

add boot-datomic and your preferred logging framework to the environment.

```clojure
(set-env!
  :source-paths #{"src"}
  :dependencies '[[tailrecursion/boot-datomic     "0.1.0-SNAPSHOT"] 
                 ;[org.slf4j/slf4j-nop            "1.7.7"]
                  [ch.qos.logback/logback-classic "1.0.1"] ]
  :repositories #(into % [["datomic"   {:url      "https://my.datomic.com/repo"
                                        :username (System/getenv "DATOMIC_REPO_USERNAME")
                                        :password (System/getenv "DATOMIC_REPO_PASSWORD") }]]))

(require
  '[tailrecursion.boot-datomic :refer [datomic]] )

(deftask run
  []
  (comp (wait) (speak) (datomic :license-key (System/getenv "DATOMIC_LICENSE_KEY")) ))
```
