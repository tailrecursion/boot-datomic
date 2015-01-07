# boot datomic
run the datomic transactor embedded in a boot pod

## setup

install the datomic pro jar into your local maven repository
```bash
mvn install:install-file -DgroupId=com.datomic -DartifactId=datomic-transactor-pro -Dfile=datomic-transactor-pro-0.9.5078.jar -DpomFile=pom.xml
```

add a datomic license key to your bash profile
```bash
export DATOMIC_LICENSE_KEY=<license-key>
```

add the task to your boot script
```clojure
(require 
  '[boot.core        :as core]
  '[tailrecursion.boot-datomic :refer [datomic]] )

(core/deftask run
  []
  (comp (wait) (speak) (datomic :license-key (System/getenv "DATOMIC_LICENSE_KEY")) ))
```
