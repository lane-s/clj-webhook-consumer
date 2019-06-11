(ns clj-webhook-consumer.main
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [clj-webhook-consumer.handler :refer [app]])
  (:gen-class))

(defn -main
  "Start the server on port 3000"
  [& args]
  (run-jetty app {:port 3000}))
