(ns clj-webhook-consumer.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [clj-webhook-consumer.gen-app :refer [gen-app]])
  (:gen-class))

(defn -main
  "Start the server on port 3000"
  [& args]
  (run-jetty (gen-app) {:port 3000}))
