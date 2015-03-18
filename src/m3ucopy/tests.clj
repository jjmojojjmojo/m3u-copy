(ns m3ucopy.tests
  "Tests Galore"
  (:use clojure.test)
  (:require [m3ucopy.core :as core]
            [me.raynes.fs :as fs]))

(def tempdir
  (fs/file 
    (fs/tmpdir) 
    (fs/temp-name "m3ucopy-tests-")))

(def source
  (fs/file "testdata"))

(def dest
  (fs/file tempdir "remote"))

;; fixtures
(defn testfiles
  [f]
  (fs/mkdirs dest)
  (f)
  (fs/delete-dir tempdir))

(use-fixtures :once testfiles)

(deftest extended?
  (is (core/extended? "#EXTM3U")
      "Positive test for extended M3U file")
  (is (not (core/extended? "NOT"))
      "Negative test for extended M3U file"))

(deftest extended-meta?
  (is (core/extended-meta? "#EXTINF:241,Lucas - Lucas With The Lid Off") 
      "Positive test for extended M3U line")
  (is (not (core/extended-meta? "241,Lucas - Lucas With The Lid Off"))
      "Negative test for extended M3U line"))

(deftest copy?
  (let [testpath (fs/file source "Pipe_Choir_-_09_-_Enya.mp3")]
    (is (not (core/copy? testpath testpath)) "Should not copy")))

(deftest copy?
  (let [testpath (fs/file source "Pipe_Choir_-_09_-_Enya.mp3")
        testdest (fs/file dest "fileXXXX.mp3")]
    (is (core/copy? testpath testdest) "Should copy")))

(deftest bad-chars
  (is (= (core/bad-chars "...bad\\news/bears?,.mp3")
         "_bad_news_bears_.mp3")
      "Bad characters for a file name"))

(deftest relative-to
  (is 
    (= (core/relative-to "/home/m3ucopy/good/morning" "/home/m3ucopy/playlist.m3u")
       "good/morning")
    "Simple relative path, typical case"))

(deftest smart-tags
  (is
    (= (core/smart-tags "/home/m3ucopy/fake/path.mp3")
       {:genera "Unknown"
        :artist "Unknown"
        :album "Unknown"
        :album-artist "Unknown"
        :title "Unknown"
        :extension ".mp3"
        :track "000"
       })
    "Tags on a bad path")
  (is 
    (= 
      (core/smart-tags (fs/file source "Pipe_Choir_-_09_-_Enya.mp3"))
      {:genera "Unknown"
       :artist "Pipe Choir"
       :album "microSong Entries"
       :album-artist "microSong Challenge"
       :title "Enya"
       :extension ".mp3"
       :track "009"})
    "Tags on a known tagged file"))

(deftest uid-path
  (let [test-src (fs/file source "Lee_Rosevere_-_49_-_Wormhole_Gazz.mp3")]
    (is
      (= 
        (core/uid-path test-src dest)
        (fs/file dest "517ab8d7f7d7798d2cdbb2c429fceafb.mp3"))
      "UID path for a good file")))

(deftest artist-album-name
  (let [test-src (fs/file source "Lee_Rosevere_-_49_-_Wormhole_Gazz.mp3")]
    (is
      (= 
        (core/artist-album-name test-src dest)
        (fs/file dest "Lee Rosevere - microSong Entries - 049 - Wormhole Gazz.mp3"))
      "File with artist/album in the filename")))

(deftest artist-album-path
  (let [test-src1 (fs/file source "Lee_Rosevere_-_49_-_Wormhole_Gazz.mp3")
        test-src2 (fs/file source "Zachary_Preston_-_211_-_Tiny_Knit.mp3")]
    (is 
      (=
        (core/artist-album-path test-src1 dest)
        (fs/file dest "microSong Challenge/microSong Entries/049 - Lee Rosevere - Wormhole Gazz.mp3"))
      "File with album/artist directories - compilation example")
    
    (is 
      (=
        (core/artist-album-path test-src2 dest)
        (fs/file dest "Zachary Preston/microSong Entries/211 - Tiny Knit.mp3"))
      "File with album/artist directories - no album-artist specified")))
