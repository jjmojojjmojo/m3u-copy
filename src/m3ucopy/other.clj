(ns m3ucopy.other
  (:require [me.raynes.fs :as fs])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!")
  (println args)
  (println (fs/home)))
