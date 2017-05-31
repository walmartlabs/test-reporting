(defproject com.walmartlabs/test-reporting "0.1.0"
  :description "Simple library to pretty print context when tests fail."
  :url "https://github.com/walmartlabs/test-reporting"
  :license {:name "Apache Software License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :plugins [[lein-codox "0.10.3"]]
  :codox {:source-uri "https://github.com/walmartlabs/test-reporting/blob/master/{filepath}#L{line}"
          :metadata {:doc/format :markdown}})
