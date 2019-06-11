(ns clj-webhook-consumer.handler
  (:require [ring.middleware.defaults :refer
             [wrap-defaults api-defaults]]
            [reitit.ring :as ring])
  (:gen-class))

(def hook-handler
  (ring/ring-handler
   (ring/router
    [["/" {:get (fn [_] {:status 200
                         :body "Hello world"
                         :headers
                         {"Content-Type" "text/html"}})}]])))

(def app (wrap-defaults hook-handler api-defaults))
