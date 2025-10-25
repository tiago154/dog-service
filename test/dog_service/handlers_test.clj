(ns dog-service.handlers-test
  (:require [clojure.test :refer [deftest is testing]]
            [dog-service.handlers :as handlers]
            [dog-service.dog :as dog]
            [clj-http.client :as http]))

(deftest random-dog-test
  (testing "returns 200 with the random dog payload"
    (with-redefs [dog/fetch-dog-image (constantly {:image "img"})]
      (let [resp (handlers/random-dog)]
        (is (= 200 (:status resp)))
        (is (= {:image "img"} (:body resp)))))))

(deftest dog-breeds-test
  (testing "returns 200 with the breeds list"
    (with-redefs [dog/fetch-dog-breeds (constantly ["pug" "beagle"])]
      (let [resp (handlers/dog-breeds)]
        (is (= 200 (:status resp)))
        (is (= ["pug" "beagle"] (:body resp)))))))

(deftest dog-by-breed-error-test
  (testing "propagates the error and responds 404"
    (with-redefs [dog/fetch-dog-image-by-breed (constantly {:error "erro" :status 404})]
      (let [resp (handlers/dog-by-breed "pug" false)]
        (is (= 404 (:status resp)))
        (is (= {:error "erro"} (:body resp)))))))

(deftest dog-by-breed-json-test
  (testing "returns the JSON payload for the breed"
    (with-redefs [dog/fetch-dog-image-by-breed (constantly {:image "img" :breed "pug" :source "Dog API"})]
      (let [resp (handlers/dog-by-breed "pug" false)]
        (is (= 200 (:status resp)))
        (is (= {:image "img" :breed "pug" :source "Dog API"}
               (:body resp)))))))

(deftest dog-by-breed-image-test
  (testing "returns image bytes when show-image? is true"
    (let [image-bytes (.getBytes "fake" "UTF-8")]
      (with-redefs [dog/fetch-dog-image-by-breed (constantly {:image "url" :breed "pug" :source "Dog API"})
                    http/get (fn [_ _] {:body image-bytes})]
        (let [resp (handlers/dog-by-breed "pug" true)]
          (is (= 200 (:status resp)))
          (is (= "image/jpeg" (get-in resp [:headers "Content-Type"])))
          (is (= image-bytes (:body resp))))))))
