(ns clj-webhook-consumer.gen-app
  (:require [ring.middleware.defaults :refer
             [wrap-defaults api-defaults]]
            [reitit.ring :as ring]
            [reitit.ring.middleware.parameters :refer [parameters-middleware]]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [muuntaja.core :as m]
            [yaml.core :as yaml]
            [clojure.java.shell :refer [sh]]
            [clojure.walk :refer [keywordize-keys]])
  (:gen-class))

(defn- invalid-key?
  [local-key {{request-key "key"} :query-params}]
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
  [hook {:keys [body-params query-params]}]
  (->>
   (get-env-kv-pairs (:body->env hook) (keywordize-keys body-params))
   (concat (get-env-kv-pairs (:query->env hook) (keywordize-keys query-params)))
   (into {})))

(defn- execute-scripts
  "Execute each script for `hook` with environment
  variables specified in `env`"
  [path hook env]
  (doseq [script (:script hook)]
    (sh (str path script) :env env)))

(defn- hook-handler
  [key path hook req]
  (if (invalid-key? key req) {:status 401}
      (do (execute-scripts path hook (get-env hook req))
          (println "Running script " path " with environment variables: ")
          (println (get-env hook req))
          {:status 200})))

(defn- hook->route
  "Transform `hook` into an endpoint
  that will execute the scripts associated
  with `hook` when it is hit with a valid
  `key`"
  [key path hook]
  [(str "/" (:name hook))
   (let [handler (partial hook-handler key path hook)]
     {:post handler
      :get handler})])

(defn- gen-routes
  "Generate endpoints from the config file.
  `path` is prepended to all script paths"
  [path key]
  (let [config (yaml/from-file (str path "./.clj-webhook.yaml") true)]
    (map (partial hook->route key path) (:hooks config))))

(defn gen-app
  ([path key]
   (wrap-defaults
    (ring/ring-handler
     (ring/router (gen-routes path key)
                  {:data {:muuntaja m/instance
                          :middleware [parameters-middleware
                                       muuntaja/format-middleware]}})
     (constantly {:status 404 :body "Hook not found"}))
    api-defaults))
  ([] (gen-app "" (System/getenv "CLJ_WEBHOOK_KEY"))))
