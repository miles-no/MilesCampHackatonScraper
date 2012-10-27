(ns miles-hackaton-scraper.core
  (:require [net.cgrand.enlive-html :as html]
            [cheshire.core :as json]
            [clojure.java.io :as io]
            [miles-hackaton-scraper.tokenizer :as t]
            [clojure.string :as str]))

(def bergen-emp "http://www.miles.no/menneskene/bergen")
(def oslo-emp "http://www.miles.no/menneskene/oslo")
(def stavanger-emp "http://www.miles.no/menneskene/stavanger")

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(defn links-to-people [city]
  (let [links (html/select (fetch-url city) [:#ansattListe :li (html/nth-child 2) ])]
    (reduce (fn [so-far i]
              (conj so-far (get-in i [:attrs :href]))) [] links)))

(defn extract-role [text]
  (let [string (second (re-find #"Rolle:\s(.+)$" text))]
    (map #(-> % (.trim) (.toLowerCase))(seq (.split string "og|,")))))

(defn extract-bio [text]
  (.replaceAll (.replaceAll text "\n" "") "[\\s]+" " "))

(defn get-info [link city]
  (let [page (fetch-url link)
        image-link (get-in (first (html/select page [:.ansattbildeContainer :img])) [:attrs :src])
        id  (hash image-link)
        full-name (first (html/select page [:#medarbeiderVisning :> :h2 html/content]))
        role (extract-role (str/join (html/select page [:#medarbeiderVisning :> :p html/html-content])))
        bio (extract-bio (str/join (html/select page [:#medarbeiderVisning :.textController :p :> html/content])))]
    {:id id :name full-name :image image-link :role role :bio bio :branch city}))

(defn get-branch [branch city]
  (reduce
   (fn [so-far i] (conj so-far (get-info i city)))
   [] (links-to-people branch)))


(defn scrape-emps []
  (let [oslo (get-branch oslo-emp "Oslo")
        bergen (get-branch bergen-emp "Bergen")
        stavanger (get-branch stavanger-emp "Stavanger")]
    (concat oslo bergen stavanger)))


(defn extract-all []
  (let [emps (scrape-emps)]
    (t/add-tags emps)))

(defn print-file [name data]
  (with-open [wrtr (io/writer name)]
    (.write wrtr data)))

(defn dump-json [filename]
  (print-file filename (json/generate-string (extract-all))))