(ns dog-service.routes
  (:require [compojure.api.sweet :refer [api context GET ANY]]
            [dog-service.handlers :as handlers]
            [ring.util.http-response :refer [ok not-found]]
            [schema.core :as s]))

(s/defschema DogImage
  {:image s/Str
   :breed s/Str
   :source s/Str})

(s/defschema ErrorResponse
  {:error s/Str})

(def app
  (api
   {:swagger
    {:ui "/docs"
     :spec "/swagger.json"
     :data {:info {:title "Dog Service API"
                   :description "API para buscar imagens aleatórias de cachorros"}
            :tags [{:name "dogs" :description "Endpoints relacionados a cachorros"}]}}}

   (GET "/favicon.ico" []
     :no-doc true
     (not-found {:error "Not Found"}))

   (GET "/" []
     :no-doc true
     (ok {:status "UP"}))

   (context "/api" []
     :tags ["dogs"]

     (GET "/dog" []
       :return DogImage
       :summary "Retorna uma imagem aleatória de cachorro"
       :responses {200 {:schema DogImage}
                   500 {:schema s/Any}}
       (handlers/random-dog))

     (GET "/dogs" []
       :return [s/Str]
       :summary "Lista todas as raças disponíveis"
       :responses {200 {:schema [s/Str]}
                   500 {:schema s/Any}}
       (handlers/dog-breeds))

     (GET "/dogs/:breed" []
       :path-params [breed :- s/Str]
       :query-params [{showImage :- s/Bool false}]
       :summary "Retorna uma imagem de uma raça específica"
       :description "Se `showImage=true`, retorna a imagem diretamente no navegador; caso contrário, retorna JSON com dados da imagem."
       :responses {200 {:schema s/Any
                        :examples {:json {:image "url_da_imagem" :breed "bulldog" :source "Dog API"}
                                   :image "image/jpeg bytes"}}
                   404 {:schema ErrorResponse}
                   500 {:schema s/Any}}
       (handlers/dog-by-breed breed showImage)))

   (ANY "*" []
     :no-doc true
     (not-found {:error "Not Found"}))))
