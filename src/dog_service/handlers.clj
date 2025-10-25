(ns dog-service.handlers
  (:require [clj-http.client :as http]
            [dog-service.dog :as dog]
            [ring.util.http-response :refer [ok not-found]]
            [ring.util.response :refer [response content-type]]))

(defn image-response [bytes]
  (-> (response bytes)
      (content-type "image/jpeg")))

(defn random-dog []
  (ok (dog/fetch-dog-image)))

(defn dog-breeds []
  (ok (dog/fetch-dog-breeds)))

(defn dog-by-breed [breed show-image?]
  (let [result (dog/fetch-dog-image-by-breed breed)
        show? (boolean show-image?)]
    (cond
      (:error result)
      (not-found {:error (:error result)})

      show?
      (let [img-bytes (:body (http/get (:image result) {:as :byte-array}))]
        (image-response img-bytes))

      :else
      (ok result))))
