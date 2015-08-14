(ns collider.projections
  (:use muon-clojure.client)
  (:require [serializable.fn :as sfn]
            [clojure.core.async :refer [<! chan go]]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json]
            [collider.common :as common]
            [clj-rhino :as js]))

(def photon (muon-client "amqp://localhost" "photon-client" "photon-client" "client"))
(def queries (ref {})) ;; TODO: Make persistent!

(defn next-avg [avg x n] (double (/ (+ (* avg n) x) (inc n))))

;; Code handling
;;;;;;;;;;;;;;;;

(defmulti generate-function (fn [lang _] (name lang)))

(defmethod generate-function "clojure" [lang f-string]
  (let [code (eval (let [f (read-string f-string)]
                     (if (= (first f) 'fn)
                       (conj (rest f) 'serializable.fn/fn)
                       f)))]
    {:computable code
     :persist f-string}))

(defn generate-fun-with-return [scope fun]
  (fn [& args]
    (let [res (apply js/call-timeout scope fun 9999999 args)]
      (try
        (json/read-str res :key-fn keyword)
        (catch Exception e res)))))

(defmethod generate-function "javascript" [lang f]
  (let [sc (js/new-safe-scope)
        compiled-fun (js/compile-function sc f :filename (str (common/uuid) ".js"))
        fun-with-return (generate-fun-with-return sc compiled-fun)]
    {:computable fun-with-return
     :persist f}))

(extend org.bson.types.ObjectId js/RhinoConvertible
  {:-to-rhino (fn [obj scope ctx] (str obj))})

(defn register-query! [projection-name stream-name lang f init]
  (let [s-name (if (nil? stream-name) "__all__" stream-name) 
        function-descriptor (generate-function lang f)
        function (:computable function-descriptor)
        s (with-muon photon (stream-subscription "muon://photon/stream"
                                                 :from 0
                                                 :stream-type "hot-cold"
                                                 :stream-name stream-name))
        running-query (ref {:projection-name projection-name
                            :fn (:persist function-descriptor)
                            :stream-name s-name
                            :language lang
                            :current-value init
                            :processed 0
                            :last-event nil
                            :last-error nil
                            :avg-time 0
                            :status :running})]
    (dosync (alter queries assoc projection-name running-query))
    (go
      (loop [current-value init current-event (<! s)]
        (if (nil? current-event)
          (dosync (alter running-query assoc :status :finished))
          (let [start-ts (System/currentTimeMillis)
                new-value (try
                            (function current-value current-event)
                            (catch Exception e
                              (log/info (.getMessage e))
                              (.printStackTrace e)
                              e))
                current-time (- (System/currentTimeMillis) start-ts)]
            (if (instance? Exception new-value)
              (dosync
                (alter running-query
                       merge {:last-event current-event
                              :avg-time (next-avg
                                          (:avg-time @running-query)
                                          current-time
                                          (:processed @running-query))
                              :processed (inc (:processed @running-query))
                              :last-error new-value 
                              :status :failed}))                
              (do
                (dosync
                  (alter running-query
                         merge {:last-event current-event
                                :avg-time (next-avg
                                            (:avg-time @running-query)
                                            current-time
                                            (:processed @running-query))
                                :current-value new-value
                                :processed (inc (:processed @running-query))}))
                (recur new-value (<! s))))))))))

