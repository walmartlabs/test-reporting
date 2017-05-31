(ns user
  (:require
    [clojure.test :refer [is deftest run-tests]]
    [com.walmartlabs.test-reporting :refer [reporting]]))


(deftest example-single-symbol-reporting
  (let [response {:status 404 :body "NOT FOUND"}]
    (reporting response
      (is (= 200 (:status response))))))

(comment
  (run-tests)
  )
