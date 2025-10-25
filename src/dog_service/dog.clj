(ns dog-service.dog
  (:require [clj-http.client :as http]
            [clojure.string :as str]))

(def api-base "https://dog.ceo/api")

(defn breed->api-path [breed]
  (str/replace breed #"-" "/"))

(defn fetch-dog-image []
  (let [resp (http/get (str api-base "/breeds/image/random") {:as :json})]
    {:image (get-in resp [:body :message])
     :breed "random"
     :source "Dog API"}))

(defn fetch-dog-breeds []
  (->> (get-in (http/get (str api-base "/breeds/list/all") {:as :json})
               [:body :message])
       (mapcat (fn [[breed subs]]
                 (let [b (name breed)]
                   (if (seq subs)
                     (map #(str b "-" %) subs)
                     [b]))))
       sort))

(defn fetch-dog-image-by-breed [breed]
  (let [url (str api-base "/breed/" (breed->api-path breed) "/images/random")]
    (try
      (let [resp (http/get url {:as :json})]
        {:image (get-in resp [:body :message])
         :breed breed
         :source "Dog API"})
      (catch clojure.lang.ExceptionInfo e
        (let [status (-> e ex-data :status)]
          {:error (if (= status 404)
                    "Raça não encontrada"
                    "Erro na Dog API")
           :status status})))))
