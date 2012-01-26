(ns ^{:doc "Remove unecessary images"}
  w-import-gen.image
  (:use [midje.sweet]
        [clojure-csv.core :only [parse-csv]]
        [clojure.repl :only [doc]])
  (:require [w-import-gen.io.util :as u]))

(unfinished )

(defn get-img-ids "Given a line of contents, extract all ids for the pictures attribute."
  [l]
  (set (map second (filter (fn [[a p]] (= a "pictures")) (partition-all 2 (clojure.string/split l #";"))))))

(fact "get-img-ids"
  (get-img-ids "a;b;c;d;pictures;id1;pictures;id2;some;noise;pictures;id3") => #{"id1", "id2", "id3"})

(defn get-img-id "Given a line of the picture file, retrieve the 3rd entry which is the image id."
  [l] 
  (nth (.split l ";") 2))

(fact "get-img-id"
      (get-img-id "a;b;c;d...") => "c")

(defn read-content! "Retrieve all the images ids from the content file."
  [content-file]
  (set (mapcat #(get-img-ids %) (u/read-lines content-file))))

(fact "read-content!"
  (read-content! :content-file) => #{:img-id1 :img-id2 :img-id3}
  (provided
    (u/read-lines :content-file) => [:line1 :line2]
    (get-img-ids :line1) => #{:img-id1 :img-id2}
    (get-img-ids :line2) => #{:img-id3}))

(defn filter-img "Filter the images in the img-file which are indeed used in the img-ids set."
  [img-file img-ids]
  (filter #(when (not (nil? (img-ids (get-img-id %)))) %) (u/read-lines img-file)))

(fact "filter-img"
  (filter-img :img-file #{:id1 :id4}) => [:line1 :line4]
  (provided
    (u/read-lines :img-file) => [:line1 :line2 :line3 :line4]
    (get-img-id :line1) => :id1
    (get-img-id :line4) => :id4
    (get-img-id :line2) => :id2
    (get-img-id :line3) => :id3))

(defn filter-img! "Filter the images which are used"
  [img-file content-file out-file]
  (let [img-ids-from-content (read-content! content-file)]
    (u/lazy-write-lines out-file (filter-img img-file img-ids-from-content))))

(fact
 (filter-img! :img-file :content-file :out-file) => nil
 (provided
  (read-content! :content-file)                  => :content
  (filter-img :img-file :content)                => :filtered-lines
  (u/lazy-write-lines :out-file :filtered-lines) => nil))
