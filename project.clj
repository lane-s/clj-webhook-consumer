(defproject clj-webhook-consumer "0.1.0-SNAPSHOT"
  :description "An easily configurable server for consuming webhooks by running shell scripts"
  :url "https://github.com/lane-s/clj-webhook-consumer"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [metosin/reitit "0.3.7"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-jetty-adapter "1.7.1"]]
  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler clj-webhook-consumer.handler/app }
  :main ^:skip-aot clj-webhook-consumer.core
  :target-path "target/%s"
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring/ring-mock "0.3.2"]]}
             :uberjar {:aot :all
                       :main clj-webhook-consumer.main}})
