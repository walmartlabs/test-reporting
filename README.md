# com.walmartlabs/test-reporting

Supplements `clojure.test/is` by pretty-printing some context when a test fails.

[![Clojars Project](https://img.shields.io/clojars/v/com.walmartlabs/test-reporting.svg)](https://clojars.org/com.walmartlabs/test-reporting)

[API Documentation](http://walmartlabs.github.io/apidocs/test-reporting/)

## Usage

test-reporting adds a single macro, `reporting`, that can be integrated into
your tests:

```clj
(require '[com.walmartlabs.test-reporting :refer [reporting]])

(let [response (get-response)]
  (reporting response
    (is (= 200 (:status response))))
```

In the event that the `is` test fails, the `response` will be pretty-printed to the console:

```
FAIL in (example-single-symbol-reporting) (user.clj:10)
expected: (= 200 (:status response))
  actual: (not (= 200 404))
 context:
 {response {:status 404, :body "NOT FOUND"}}
```


See the full API: the first form to `reporting` may be a single symbol, a map, or a vector of symbols.

## License

Copyright © 2017 Walmart

Distributed under the Apache Software License 2.0.

