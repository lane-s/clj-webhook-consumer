(defproject clj-webhook-consumer "0.1.0-SNAPSHOT"
  :description "An easily configurable server for consuming webhooks by running shell scripts"
  :url "https://github.com/lane-s/clj-webhook-consumer"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]]
  :main ^:skip-aot clj-webhook-consumer.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
