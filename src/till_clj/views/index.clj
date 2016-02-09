(ns till-clj.views.index
  (:require [clojure.string :as str]
            [hiccup.page :as hic-p]
            [till-clj.views.helpers :as vh]))

(defn home-page
  []
  (hic-p/html5
    (vh/title-banner "Home")
    [:h1 "Home"]
    [:p "Welcome to Till-Clj, a Clojure Webapp built on Compojure for handling orders and printing receipts for restaurants!"]))
