(require '[boot.pod :as pod])
(require '[clojure.string :as string])

(set-env!
  :source-paths #{"src"}
  :dependencies '[[me.raynes/fs "1.4.6"]
                  [pandect "0.5.1"]
                  [green-tags "0.3.0-alpha"]])

(deftask build
  "Construct a standalone jar file")
  
