(require '[boot.pod :as pod])
(require '[clojure.string :as string])
(require '[clojure.test :as testing])

(set-env!
  :source-paths #{"src"}
  :dependencies '[[me.raynes/fs "1.4.6"]
                  [pandect "0.5.1"]
                  [green-tags "0.3.0-alpha"]])

(deftask build
  "Construct a standalone jar file")

(deftask test
  "Run the tests"
  []
  (require '[m3ucopy.tests])
  (testing/run-tests 'm3ucopy.tests))
