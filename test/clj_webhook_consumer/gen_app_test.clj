(ns clj-webhook-consumer.gen-app-test
  (:require [clojure.test :refer :all]
            [clj-webhook-consumer.gen-app :refer :all]
            [ring.mock.request :as mock]
            [clojure.java.io :as io]))

(def test-path "./test/clj_webhook_consumer/")
(def out-file-1 (str test-path "test_out.txt"))
(def out-file-2 (str test-path "test_out_2.txt"))
(def test-env-file (str test-path "test_env_out.txt"))
(def test-query-file (str test-path "test_query_out.txt"))

(defn out-file-fixture [f]
  (spit out-file-1 "")
  (spit out-file-2 "")
  (spit test-env-file "")
  (spit test-query-file "")
  (f)
  (io/delete-file out-file-1)
  (io/delete-file out-file-2)
  (io/delete-file test-env-file)
  (io/delete-file test-query-file))

(use-fixtures :once out-file-fixture)

(def test-config-path
  (str test-path ".clj-webhook.yaml"))

(def hook-body {:repository { :full_description "Some repo"
                              :dockerfile "Docker file contents" }})

(def test-app (gen-app test-path "test_api_key"))

(defn test-request [url]
  (test-app (-> (mock/request :post url)
                (mock/json-body hook-body))))

(deftest test-generated-routes
  (let [res (test-request "/testhookA?key=test_api_key")]
    (testing "Request to testhookA succeeds"
      (is (= (:status res) 200)))

    (testing "Request to testhookA runs test.sh"
      (is (= (slurp out-file-1) "Test\n")))

    (testing "Request to testhookA runs env_test.sh with environment variables set"
      (is (= (slurp test-env-file) "Some repo\nDocker file contents\n"))))

  (let [res (test-request "/testhookB?key=test_api_key&param=testparam")]
    (testing "Request to testHookB succeeds"
      (is (= (:status res) 200)))

    (testing "Request to testHookB runs test_2.sh"
      (is (= (slurp out-file-2) "Test 2\n")))

    (testing "Request to testHookB runs query_test.sh with environment variables set"
      (is (= (slurp test-query-file) "testparam\n"))))

  (testing "Request to testHookB without key fails"
    (let [res (test-request "/testhookB")]
      (is (= (:status res) 401)))))
