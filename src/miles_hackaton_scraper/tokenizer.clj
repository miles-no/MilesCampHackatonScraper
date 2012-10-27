(ns miles-hackaton-scraper.tokenizer
  (:require [clojure.set :as set]
            [clojure.string :as str])
  (:use miles-hackaton-scraper.core))

(defn stopwords []
  (set (seq (.split (slurp "resources/stopwords.txt") "\n"))))


(defn not-blank? [str]
  (not (str/blank? str)))

(defn short? [str]
  (< 3 (.length str)))

(defn split-bio [bio]
  (filter short? (set/difference (set (str/split bio #"[\s;:\.\-\\,()/]")) (stopwords))))

(defn all-bios []
  (reduce (fn [so-far i] (assoc so-far (:id i) (split-bio (.toLowerCase (:bio i))) )) {} all))

(defn add-tags []
  (let [bios (all-bios)]
    (map
     (fn [person] (let [id (:id person)
                       bio (get bios id)]
                   (assoc person :tags bio)))
     all)))