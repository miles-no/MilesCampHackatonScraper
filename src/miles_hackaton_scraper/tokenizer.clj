(ns miles-hackaton-scraper.tokenizer
  (:require [clojure.set :as set]
            [clojure.string :as str]))

(defn stopwords []
  (set (seq (.split (slurp "resources/stopwords.txt") "\n"))))

(defn not-blank? [str]
  (not (str/blank? str)))

(defn not-short? [str]
  (< 3 (.length str)))

(defn split-bio [bio]
  (filter not-short? (set/difference (set (str/split bio #"[\s;:\.\-\\,()/]")) (stopwords))))

(defn all-bios [all-emps]
  (reduce (fn [so-far i] (assoc so-far (:id i) (split-bio (.toLowerCase (:bio i))) )) {} all-emps))

(defn add-tags [all-emps]
  (let [bios (all-bios all-emps)]
    (map
     (fn [person] (let [id (:id person)
                       bio (get bios id)]
                   (assoc person :tags bio)))
     all-emps)))