; Copyright (c) 2017-present Walmart, Inc.
;
; Licensed under the Apache License, Version 2.0 (the "License")
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;     http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.

(ns com.walmartlabs.test-reporting
  "An extension for clojure.test that allows additional context to be output only
when a test failure or error occurs."
  (:require
    #?(:clj  [clojure.test :refer [*report-counters* *initial-report-counters*]]
       :cljs [clojure.test :refer [get-current-env]])
    [clojure.pprint :as pprint]))

(defn ^:private pretty-print
  "Pretty-prints the supplied object to a returned string."
  [object]
  (pprint/write object
                :stream nil
                :pretty true))

(def ^:dynamic *reporting-context*
  "Contains a map of additional information to be printed in the report
  when a test failure or error is observed."
  nil)

(def *reported?
  "Used to prevent multiple reports of the context. This flag is set
  to true when [[*reporting-context*]] is bound with new data, and reset to
  false when the context is printed out."
  (atom false))

(defn snapshot-counters
  "Returns just the :fail and :error keys from the `*report-counters*` var
  (or the :report-counters key of the cljs.test environment).
  A change to either of these values indicates a test failure, triggering the
  reporting of context."
  []
  (select-keys
    #?(:clj  @*report-counters*
       :cljs (:report-counters (get-current-env)))
    [:fail :error]))

(defn report-context
  []
  (when *reporting-context*
    (println " context:\n" (pretty-print *reporting-context*))))

(defmacro ^:private mcase
  "A macro to provide a reader-conditional-like functionality inside of a macro definition.
  This was retrieved from https://github.com/cgrand/macrovich/blob/master/src/net/cgrand/macrovich.cljc"
  [& {:keys [cljs clj]}]
  (if (contains? &env '&env)
    `(if (:ns ~'&env) ~cljs ~clj)
    (if #?(:clj (:ns &env) :cljs true)
      cljs
      clj)))

(defmacro reporting
  "Establishes a context in which certain data is printed
  when the form (tests using the `is` macro)
  identify test failures or exceptions.

  This adds keys to the [[*reporting-context*]].
  After executing the forms, a check is made to see if
  the number of errors or failures changed; if so
  then the reporting context is pretty-printed to `*out*`.

  The data maybe a symbol: The unevaluated symbol becomes
  the key, and the evaluated symbol is the value.

  Alternately, data may be a map, which is merged into the context.
  In this form, keys must be quoted if symbols.

      (reporting request
          (is ...))

  is the same as:

      (reporting {'request request}
         (is ...))

  A final alternative is to report a vector; each of the symbols
  is quoted.

      (reporting [request response] ...)

  is the same as:

      (reporting {'request request 'response response} ...)

  Nested usages of reporting is allowed; a reasonable attempt
  is made to prevent the context from being printed multiple times when there
  are multiple failures. Typically, the context is only printed once, at the
  deepest nested block in which test failures occur."
  [data & forms]
  (cond

    (symbol? data)
    `(reporting {(quote ~data) ~data} ~@forms)

    (vector? data)
    `(reporting ~(into {} (map #(vector (list 'quote %) %)) data) ~@forms)

    (not (map? data))
    (throw (ex-info "com.walmartlabs.test-reporting/reporting - data must be a symbol, vector or a map"
                    {:data data :forms forms}))

    :else
    (let [body `(binding [*reporting-context* (merge *reporting-context* ~data)]
                  (let [counters# (snapshot-counters)]
                    (try
                      ;; New values have been bound into *reporting-context* that need
                      ;; to be reported on a failure or error.
                      (reset! *reported? false)
                      ~@forms
                      (finally
                        (when (and (not @*reported?)
                                   (not= counters# (snapshot-counters)))
                          ;; Don't do further reporting while unwinding, and don't
                          ;; try to report the context a second time if there's an exception
                          ;; the first time. It is expected that some of the context values
                          ;; will be quite large, so we want to ensure that they are not
                          ;; pretty-printed multiple times.
                          (reset! *reported? true)
                          ;; The point here is to call report-context at the deepest level,
                          ;; so all the keys can appear together. Looks better (due to
                          ;; indentation rules). However, it would be a lot simpler to just
                          ;; let each reporting block track its own keys/values in a local
                          ;; symbol.
                          (report-context))))))]
      (mcase :clj `(binding [*report-counters* (or *report-counters*
                                                   (ref *initial-report-counters*))]
                     ~body)
             :cljs body))))
