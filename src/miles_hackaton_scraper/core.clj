(ns miles-hackaton-scraper.core
  (:require [net.cgrand.enlive-html :as html]))

(def *base-url* "http://miles.no")

(def *bergen-people* "http://www.miles.no/menneskene/bergen")
(def *oslo-people* "http://www.miles.no/menneskene/oslo")
(def *stavanger-people* "http://www.miles.no/menneskene/stavanger")

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(defn people [city]
  (let [links (html/select (fetch-url city) [:#ansattListe :li (html/nth-child 2) ])]
    (map second (reduce (fn [so-far i]
              (let [name (first (:content i)) 
                    url (get-in i [:attrs :href])]
                (assoc so-far name url))) {} links))))

(defn extract-role [text]
  (let [string (second (re-find #"Rolle:\s(.+)$" text))]
    (map #(-> % (.trim) (.toLowerCase))(seq (.split string "og|,")))))

(defn extract-bio [text]
  (.replaceAll (.replaceAll text "\n" "") "[\\s]+" " "))

(defn get-info [link]
  (let [page (fetch-url link)
        details (html/select (fetch-url link) [:#medarbeiderVisning :p])
        image-link (get-in (first (html/select page [:.ansattbildeContainer :img])) [:attrs :src])
        full-name (first (html/select page [:#medarbeiderVisning :h2 html/content]))
        role (extract-role (first (:content (first details))))
        bio (extract-bio (first (:content (second details))))]
    {:name full-name :image image-link :role role :bio bio}))

(defn get-branch [branch]
  (reduce (fn [so-far i] (conj so-far (get-info i))) [] (people branch)))


(defn extract-all []
  (let [oslo (get-branch *oslo-people*)
        bergen (get-branch *bergen-people*)
        stavanger (get-branch *stavanger-people*)]
    {:oslo oslo :bergen bergen :stavanger stavanger}))

(def all (extract-all))