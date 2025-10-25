(ns dog-service.dog-test
  (:require [clojure.test :refer [deftest is testing]]
            [dog-service.dog :as dog]
            [clj-http.client :as http]))

(deftest breed->api-path-test
  (testing "hyphen becomes slash"
    (is (= "hound/afghan" (dog/breed->api-path "hound-afghan"))))
  (testing "string without hyphen unchanged"
    (is (= "pug" (dog/breed->api-path "pug")))))

(deftest fetch-dog-image-test
  (with-redefs [http/get (fn [_ _] {:body {:message "image-url"}})]
    (is (= {:image "image-url" :breed "random" :source "Dog API"}
           (dog/fetch-dog-image)))))

(deftest fetch-dog-breeds-test
  (with-redefs [http/get (fn [_ _]
                           {:body {:message {:hound ["afghan" "basset"]
                                              :pug []}}})]
    (is (= ["hound-afghan" "hound-basset" "pug"]
           (dog/fetch-dog-breeds)))))

(deftest fetch-dog-image-by-breed-success-test
  (with-redefs [http/get (fn [_ _] {:body {:message "breed-img"}})]
    (is (= {:image "breed-img" :breed "beagle" :source "Dog API"}
           (dog/fetch-dog-image-by-breed "beagle")))))

(deftest fetch-dog-image-by-breed-error-test
  (with-redefs [http/get (fn [_ _] (throw (ex-info "Not found" {:status 404})))]
    (is (= {:error "Raça não encontrada" :status 404}
           (dog/fetch-dog-image-by-breed "unknown"))))
  (with-redefs [http/get (fn [_ _] (throw (ex-info "Server" {:status 500})))]
    (is (= {:error "Erro na Dog API" :status 500}
           (dog/fetch-dog-image-by-breed "beagle")))))
