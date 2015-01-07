(require '[boot.pod :as pod])
(require '[clojure.string :as string])

(set-env!
  :source-paths #{"src"}
  :dependencies '[[me.raynes/fs "1.4.6"]]
  :main "m3ucopy.core/-main")

(task-options!
  jar {:main 'm3ucopy.core})

(deftask run
  "Run an entry point in the current project"
  [m  entry-point  ENTRY  str   "Entry point to run"
   a  args ARGS str "CLI arguments to pass to the entry point (wrap in quotes)"]
  (println *opts*)
  (println (get *opts* :args ""))
  (fn [next-handler]
    (fn [fileset]
      (let [env (get-env)
            runner (pod/make-pod env)
            entry-point (get *opts* :entry-point (get env :main))
            args (string/split (get *opts* :args "") #" ")]
        (aot :all)
        (println args)
        (.invoke runner entry-point (into-array args))
        (next-handler fileset)))))

(deftask build
  "Construct a standalone jar file")
  
