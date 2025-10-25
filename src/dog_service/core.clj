(ns dog-service.core
  (:require [dog-service.routes :as routes]
            [ring.adapter.jetty :as jetty])
  (:gen-class))

(def app routes/app)

(defn -main []
  (println "Server running on http://localhost:3000")
  (println "Swagger UI available at http://localhost:3000/docs")
  (jetty/run-jetty app {:port 3000 :join? false}))
