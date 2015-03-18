(ns m3ucopy.core
  "M3U file copier utility"
  (:require [clojure.java.io :as io]
            [pandect.algo.md5 :refer [md5 md5-file]]
            [me.raynes.fs :as fs]
            [green-tags.core :refer [get-fields]]
            [clojure.string :as string])
  (:gen-class))

(defmethod print-method java.io.File [o ^java.io.Writer w]
  (.write w (.getPath o)))


(defn extended?
  [line]
  "Return true if the line is a header indicating this is an *extended* M3U file"
  (.contains line "#EXTM3U"))

(defn extended-meta?
  [line]
  "Return true if the line is extended metadata, false if not"
  (boolean (re-find #"#EXTINF" line)))

^{:todo "Is this the best way to determine if the files are different?"}
(defn copy? 
  [source dest]
  "Return true if dest doesn't exist, or dest is a different file, false if not.
   Determines when to copy a file."
  (if (fs/exists? dest)
    (not= (md5-file source) (md5-file dest))
    true))

(defn bad-chars
  [filename &{:keys [replacement] :or {replacement "_"}}]
  "Given a filename, return that name with commonly bad file system chars replaced
   with :replacement character (defaults to underscore)"
  (string/replace filename #"^[.]+|[\\/?,]+" replacement))

;; custom destination paths, revisit later.. 
;; (defn expand-interp
;;   "given the list of values and a {x} template variable, return a string with
;;    the current value of x"
;;   [interp values]
;;   (let [match (re-find #"\{.+\}" interp)]
;;     (if match
;;       (get values match "X")
;;       interp)))
;; 
;; (defn interpolate
;;   "Given a template, replace {x} with the value of x and return the string"
;;   [template values]
;;   (println values)
;;   (let [words (string/split template #"\W")
;;         processed (map #(expand-interp %1 values) words)]
;;     (println words)
;;     (string/join processed " ")))
;; 
;; (defn template-path
;;   [path dest-dir template]
;;   "Create a custom destination path using tag information from the provided path"
;;   (let [metadata (smart-tags path)
;;         interp (interpolate template metadata)
;;       (println "TEMPL: " (.getPath destpath))))

(defn relative-to
  "Return a path relative to a given file"
  [path relative-to]
  (let [absrel (.getPath (fs/absolute (fs/parent relative-to)))
        abspath (.getPath (fs/absolute path))]
    (string/replace (string/replace-first abspath absrel "") #"^/" "")))

(defn smart-tags
  "Based on a music file path, return a map containing essential information 
   about the file, handles untagged information gracefully"
   [path &{:keys [default] 
           :or {default "Unknown"}}]
   (let [metadata (get-fields path)]
     {:genera (:genera metadata default)
      :artist (:artist metadata default)
      :album (:album metadata default)
      :album-artist (:album-artist metadata (:artist metadata default))
      :title (:title metadata default)
      :extension (fs/extension path)
      :track (format "%03d" (Integer/parseInt (:track metadata "0")))
     }))
   

(defn uid-path
  [path dest-dir]
  "Create a unique short 'fingerprint' file name based on tag information. 
   Useful for filesystems that can't handle special characters or long filenames"
  (let [{:keys [:album-artist :album :title :track :extension]} (smart-tags path)
        hash (md5 (str album-artist album track title))]
       (io/file dest-dir (str hash extension))))
    

(defn artist-album-name
  [path dest-dir]
  "Construct a path where the base file name contains the artist, album and track number"
  (let [{:keys [:album-artist :artist :album :title :track :extension]} (smart-tags path)
        parts [artist album track title]
        filename (str (string/join " - " parts) extension)]
    (io/file dest-dir (bad-chars filename))))

(defn artist-album-path
  [path dest-dir]
  "Create a standard 'Artist/Album/00X-Title.ext' style destination path"
  (let [{:keys [:album-artist :artist :album :title :track :extension]} (smart-tags path)
        ;; Don't add the artist unless this is a compilation and the artist 
        ;; differs from the album-artist
        parts (if (not= album-artist artist) [track artist title] [track title])
        filename (str (string/join " - " parts) extension)]
    (io/file dest-dir 
       (bad-chars album-artist) 
       (bad-chars album) 
       (bad-chars filename))))

(defn- construct-path
  [path dest-dir &{:keys [style] :or {style :same}}]
  "Create a destination path for a given file"
  (case style
    :artist-album (artist-album-name path dest-dir)
    :artist-album-path (artist-album-path path dest-dir)
    :uid (uid-path path dest-dir)
    (io/file dest-dir (fs/base-name path))))
  
(defn dest-path
  [path dest-dir &{:keys [style spaces-ok] :or {spaces-ok true style :same}}]
  "Construct a destination path based on the source path and destination 
   directory"
  (let [raw (construct-path path dest-dir :style style)]
     (if spaces-ok
       raw
       (string/replace raw #"\s+" "_"))))
    
(defn copy-music
  [source dest &{:keys [dry-run] :or {dry-run false}}]
  "Copy music file to its destination, but only if it's not already there"
  (if (copy? source dest)
    (do 
       (println "Copying" source "to" dest)
       (if (not dry-run) (fs/copy+ source dest)))
    (println dest "already exists. Skipping...")))

(defn- write-line
  [wtr line]
  "Helper function to write a single line to the given m3u file"
  (.write wtr (str line "\n")))

(defn copy-m3u
  [path dest &{:keys [style spaces-ok dry-run] 
               :or {style nil 
                    spaces-ok true 
                    dry-run false}}]
  "Reconstruct an m3u file from the given path to a destination directory, and
   copy all referenced files, mangling paths so they are relative to the 
   destination m3u file.
   
   keyword arguments control behavior:
       :path-style - the algorithm to use to generate the destination path name
                     for music files. 
                     
                     Options are:
                        - uid: generate a hash based on the id3 tags - use this
                               if the destination file system or player doesn't
                               support long file names or special characters.
                        - album-artist-name: artist - album - track - title.mp3
                        - album-artist: artist/album/track-title.mp3
                        
                     The default is to keep the file name from the source path.
                     
       :spaces-ok - set to true to leave spaces in file names. Defaults to true.
                    Spaces are replaced with underscores.
                    
       :dry-run - print what would be done without actually doing it. Defaults 
                  to false. 
       
   
   "
  (if dry-run
    (do 
      (println "****DRY RUN MODE DETECTED.*********************************")
      (println "    NO FILES WILL BE MANIPULATED")
      (println)))
   
  (if-not (fs/exists? path)
    (throw (Exception. (str "File " path " does not exist"))))
  
  (if (and (fs/exists? dest) (not (fs/directory? dest)))
    (throw (Exception. (str "Destination path " dest " is not a directory"))))
  
  (if (not (fs/exists? dest))
    (do
      (println "Creating directory " dest)
      (if dry-run (fs/mkdirs dest))))
  
  (let [temp-path (fs/temp-name "m3ucopy-")
        dest-m3u-path (io/file dest (fs/base-name path))
        dest-m3u (if dry-run temp-path dest-m3u-path)]
    (println "Creating m3u file at:" dest-m3u)
    (println "Reading m3u file from:" path)
    (println)
    (with-open [rdr (io/reader path)
                wtr (io/writer dest-m3u)]
      (let [lines (line-seq rdr)
            extended (extended? (first lines))]
        (if extended
          (do
            (write-line wtr "#EXTM3U")
            (doseq [data (partition-all 2 (rest lines))]
              (let [metadata (first data)
                    filepath (second data)
                    destpath (dest-path filepath dest :spaces-ok spaces-ok :style style)
                    playpath (relative-to destpath dest-m3u-path)]
                (copy-music filepath destpath :dry-run dry-run)
                (write-line wtr metadata)
                (write-line wtr playpath))))
          
          (doseq [line lines]
            (let [filepath line
                  destpath (dest-path filepath dest :spaces-ok spaces-ok :style style)
                  playpath (relative-to destpath dest-m3u-path)]
              (copy-music filepath destpath :dry-run dry-run)
              (write-line wtr playpath))))))
    
    (if dry-run
      (do
        (println)
        (println "******* Dumping temporary M3U file. *********")
        (print (slurp dest-m3u))
        (println "*********************************************\n")
        (fs/delete dest-m3u)))))


