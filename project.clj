(defproject clj-webhook-consumer "0.1.0-SNAPSHOT"
  :description "An easily configurable server for consuming webhooks by running shell scripts"
  :url "https://github.com/lane-s/clj-webhook-consumer"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [metosin/reitit "0.3.7"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-jetty-adapter "1.7.1"]
                 [org.flatland/ordered "1.5.7"]
                 [io.forward/yaml "1.0.9"]
                 [metosin/muuntaja "0.6.4"]]
  :main ^:skip-aot clj-webhook-consumer.core
  :target-path "target/%s"
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring/ring-mock "0.3.2"]]}
             :uberjar {:aot :all
                       :main clj-webhook-consumer.core}})
