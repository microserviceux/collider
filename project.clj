(defproject collider "1.1.0-SNAPSHOT"
  :description "Collider"
  :url "https://github.com/photonevents/collider"
  :min-lein-version "2.0.0"
  :repositories [["muoncore" "http://dl.bintray.com/muoncore/muon-java"]]
  :dependencies [[org.clojure/clojure "1.7.0-beta3"]
                 [org.clojure/clojurescript "0.0-3269"]
                 [io.muoncore/muon-clojure "0.1.19"]
                 [clj-http "1.1.2"]
                 [org.clojure/data.json "0.2.6"]
                 [io.muoncore/muon-core "0.33"]
                 [io.muoncore/muon-transport-amqp "0.33"]
                 [io.muoncore/muon-discovery-amqp "0.33"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [stylefruits/gniazdo "0.4.0"]
                 [org.marianoguerra/clj-rhino "0.2.2"]
                 [compojure "1.3.4"]
                 [fipp "0.6.2"]
                 [org.clojure/tools.logging "0.3.1"]
                 [tailrecursion/cljson "1.0.7"]
                 [org.slf4j/slf4j-log4j12 "1.7.12"]
                 [clj-http "1.1.2"]
                 [cljs-http "0.1.35"]
                 [org.clojure/java.data "0.1.1"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/data.xml "0.0.8"]
                 [serializable-fn "1.1.4"]
                 [http-kit "2.1.18"]
                 [jayq "2.5.4"]
                 [org.omcljs/om "0.8.8"]
                 [jarohen/chord "0.6.0"]
                 [clj-time "0.9.0"]
                 [ring "1.4.0"]
                 [ring/ring-json "0.3.1"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [org.json/json "20141113"]
                 [ring/ring-defaults "0.1.2"]
                 [midje "1.6.3"]
                 [uap-clj "1.0.1"]
                 [stylefruits/gniazdo "0.4.0"]]
  :main collider.core ;; http-kit
  :cljsbuild {:builds [{:source-paths ["src-cljs"]
                        :figwheel true
                        :compiler {:main collider.ui.frontend
                                   :asset-path "js/out"
                                   :output-to "resources/public/js/main.js"}}]}
  :plugins [[lein-ring "0.9.6"]
            [lein-cljsbuild "1.0.5"]
            [lein-figwheel "0.3.3"]
            [cider/cider-nrepl "0.9.1"]
            [org.clojure/tools.nrepl "0.2.10"]]
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
