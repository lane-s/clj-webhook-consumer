(ns clj-webhook-consumer.gen-app
  (:require [ring.middleware.defaults :refer
             [wrap-defaults api-defaults]]
            [reitit.ring :as ring]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [muuntaja.core :as m]
            [yaml.core :as yaml]
            [clojure.java.shell :refer [sh]])
  (:gen-class))

(defn hook-to-route [path hook]
  [(str "/" (:name hook))
   {:post (fn [{:keys [body-params]}]
            ;; TODO set the env variables
            (doseq [script (:script hook)]
              (sh (str path script)))
            {:status 200})}])

(defn gen-routes [path]
  (let [config (yaml/from-file (str path "./.clj-webhook.yaml") true)]
    (map (partial hook-to-route path) (:hooks config))))

(defn gen-app
  ([path]
   (wrap-defaults
    (ring/ring-handler
     (ring/router (gen-routes path)
                  {:data {:muuntaja m/instance
                          :middleware [muuntaja/format-middleware]}}))
    api-defaults))
  ([] (gen-app "")))
