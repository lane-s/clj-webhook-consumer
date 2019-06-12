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

(defn- invalid-key?
  [local-key {request-key "key"}]
  (and local-key (not= local-key request-key)))

(defn- get-env-kv-pairs
  "Returns a sequence of key value pairs
  where the keys are the leaves of `name-map`
  and the values are the leaves of `value-map`"
  [name-map value-map]
  (reduce (fn [acc [nk nv]]
            (let [v (get value-map nk)]
              (if (map? nv)
                (concat acc (get-env-kv-pairs nv v))
                (let [env-name (if (= nv "_")
                                 (-> (str nk) (subs 1) clojure.string/upper-case)
                                 nv)]
                  (conj acc [env-name v]))))) '() name-map))

(defn- get-env
  "Get an environment variable map
  where keys are the names specified
  in the config and values are grabbed
  from the request body"
  [hook body-params]
  (->>
   (get-env-kv-pairs (:env hook) body-params)
   (into {})))

(defn- execute-scripts
  "Execute each script for `hook` with environment
  variables specified in `env`"
  [path hook env]
  (doseq [script (:script hook)]
    (sh (str path script) :env env)))

(defn- hook->route
  "Transform `hook` into an endpoint
  that will execute the scripts associated
  with `hook` when it is hit with a valid
  `key`"
  [key path hook]
  [(str "/" (:name hook))
   {:post (fn [{:keys [body-params query-params]}]
            (if (invalid-key? key query-params) {:status 401}
                (do (execute-scripts path hook (get-env hook body-params))
                {:status 200})))}])

(defn- gen-routes
  "Generate endpoints from the config file.
  `path` is prepended to all script paths"
  [path]
  (let [config (yaml/from-file (str path "./.clj-webhook.yaml") true)]
    (map (partial hook->route (:key config) path) (:hooks config))))

(defn gen-app
  ([path]
   (wrap-defaults
    (ring/ring-handler
     (ring/router (gen-routes path)
                  {:data {:muuntaja m/instance
                          :middleware [parameters-middleware
                                       muuntaja/format-middleware]}})
     (constantly {:status 404 :body "Hook not found"}))
    api-defaults))
  ([] (gen-app "")))
