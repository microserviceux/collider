(ns collider.ext
  (:require [clojure.data.json :as json]))

(defn init-extensions []
  (extend Double json/JSONWriter
    {:-write (fn [object out]
               (cond (.isInfinite object)
                     (.print out 9007199254740992.0)
                     (.isNaN object)
                     (.print out 0.0)
                     :else
                     (.print out object)))})

  (extend Exception json/JSONWriter
    {:-write (fn [object out]
               (.print out (pr-str (.getMessage object))))})

  (extend clojure.lang.AFunction json/JSONWriter
    {:-write (fn [object out]
               (.print out (pr-str object)))})

  (extend org.bson.types.ObjectId json/JSONWriter
    {:-write (fn [object out]
               (.print out (str "\"" (.toString object) "\"")))})

  (extend clojure.lang.Ref json/JSONWriter
    {:-write (fn [object out]
               (.print out (json/write-str @object)))}))

