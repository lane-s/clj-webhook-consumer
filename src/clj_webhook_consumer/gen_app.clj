(ns clj-webhook-consumer.gen-app
  (:require [ring.middleware.defaults :refer
             [wrap-defaults api-defaults]]
            [reitit.ring :as ring]
            [reitit.ring.middleware.parameters :refer [parameters-middleware]]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [muuntaja.core :as m]
            [yaml.core :as yaml]
            [clojure.java.shell :refer [sh]])
  (:gen-class))

(defn invalid-key? [local-key {request-key "key"}]
  (and local-key (not= local-key request-key)))

(defn execute-scripts [path hook]
  (doseq [script (:script hook)]
    (sh (str path script))))

(defn hook-to-route [key path hook]
  [(str "/" (:name hook))
   {:post (fn [{:keys [body-params query-params]}]
            (if (invalid-key? key query-params) {:status 401}
              (do (execute-scripts path hook)
                {:status 200})))}])

(defn gen-routes [path]
  (let [config (yaml/from-file (str path "./.clj-webhook.yaml") true)]
    (map (partial hook-to-route (:key config) path) (:hooks config))))

(defn gen-app
  ([path]
   (wrap-defaults
    (ring/ring-handler
     (ring/router (gen-routes path)
                  {:data {:muuntaja m/instance
                          :middleware [parameters-middleware
                                       muuntaja/format-middleware]}}))
    api-defaults))
  ([] (gen-app "")))
