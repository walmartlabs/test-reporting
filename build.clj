(ns build
  (:require [clojure.tools.build.api :as b]
            [net.lewisship.build :refer [requiring-invoke]]))

(def lib 'com.walmartlabs/test-reporting)
(def version "1.2")

(def jar-params {:project-name lib
                 :version version
                 :url "https://github.com/walmartlabs/test-reporting"})

(defn clean
  [_params]
  (b/delete {:path "target"}))

(defn jar
  [_params]
  (requiring-invoke net.lewisship.build.jar/create-jar jar-params))

(defn deploy
  [_params]
  (clean nil)
  (jar nil)
  (requiring-invoke net.lewisship.build.jar/deploy-jar jar-params))

(defn codox
  [_params]
  (requiring-invoke net.lewisship.build.codox/generate
                    {:project-name lib
                     :version version}))