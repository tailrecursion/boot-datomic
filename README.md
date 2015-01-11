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

add the task to your boot script
```clojure
(require
  '[tailrecursion.boot-datomic :refer [datomic]] )

(deftask run
  []
  (comp (wait) (speak) (datomic :license-key (System/getenv "DATOMIC_LICENSE_KEY")) ))
```
