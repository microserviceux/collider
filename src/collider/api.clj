(ns collider.api
  (:require [collider.projections :as projs]
            [clojure.tools.logging :as log]
            [clojure.core.async :as async]))

(defn post-projection! [request]
  (let [body request
        projection-name (:projection-name body)
        stream-name (:stream-name body)
        language (:language body)
        code (:reduction body)
        initial-value (:initial-value body)]
    (projs/register-query! (keyword projection-name)
                           stream-name
                           (keyword language)
                           code
                           (read-string initial-value))
    "Ok"))

(defn projections []
  (map
    (fn [v] (assoc v :fn (pr-str (:fn v))))
    (map #(apply dissoc (deref %) [:_id])
         (vals @projs/queries))))

(defn projection [projection-name]
  (log/info "Querying" projection-name)
  (let [res (first (filter #(= (name (:projection-name %)) projection-name)
                           (map deref (vals @projs/queries))))]
    (log/info "Result:" (pr-str res))
    (log/info "Result:" (pr-str (muon-clojure.utils/dekeywordize res)))
    res))

(defn projection-keys []
  {:projection-keys
   (map :projection-name
        (map
          (fn [v] (assoc v :fn (pr-str (:fn v))))
          (map #(apply dissoc (deref %) [:_id])
               (vals @projs/queries))))})

