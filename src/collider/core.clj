(ns collider.core
  (:gen-class)
  (:use org.httpkit.server)
  (:require [muon-clojure.server :as mcs]
            [muon-clojure.common :as mcc]
            [clojure.tools.logging :as log]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :refer [site]]
            [ring.middleware.json :as rjson]
            [ring.middleware.params :as pms]
            [ring.middleware.reload :as reload]
            [collider.api :as api]
            [collider.ext :as ext]
            [collider.common :as common]
            [clojure.core.async :refer [to-chan]]))

(ext/init-extensions)

(def mq-url "amqp://localhost")
(def service-name "collider")
(def service-tags ["projection" "server" "photon-consumer"])

(defonce photon-instance (ref nil))

(defroutes app-routes
  (GET "/projection-keys" []
       (common/wrap-json (api/projection-keys)))
  (GET "/projections" []
       (common/wrap-json (api/projections)))
  (GET "/projection/:projection-name" [projection-name]
       (common/wrap-json (api/projection projection-name)))
  (POST "/projections" request
        (api/post-projection! (:body request)))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (routes (rjson/wrap-json-body (pms/wrap-params (site app-routes))
                                {:keywords? true})))

(def reloadable-app (reload/wrap-reload #'app))

(defrecord ColliderMicroservice [m]
  mcs/MicroserviceStream
  (expose-stream! [this]
    ;; TODO: provide a proper channel generator function and an endpoint-name
    (mcc/stream-source this "endpoint-name" (fn [params] (to-chan [1 2 3 4 5]))))
  mcs/MicroserviceCommand
  (expose-post! [this]
    ;; TODO: provide proper POST listener functions
    (mcc/on-command this "post-endpoint-1" (fn [resource] "return-value-1"))
    (mcc/on-command this "post-endpoint-2" (fn [resource] "return-value-2"))))

(defn new-microservice [url] 
  (->ColliderMicroservice
      (mcs/muon url service-name service-tags)))

(defn -main [& args]
  (let [ms (new-microservice mq-url)]
    (dosync (alter photon-instance (fn [_] ms)))
    (mcs/start-server! ms)
    (let [handler (reload/wrap-reload #'app)]
      (println run-server)
      (time (run-server handler {:port 3001})))))

