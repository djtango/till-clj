(ns till-clj.views
  (:require [clojure.string :as str]
            [hiccup.page :as hic-p]
            [till-clj.db :as db]))

(defn gen-page-head
  [title]
  [:head
   [:title (str "Till: " title)]
   (hic-p/include-css "/css/styles.css")])

(def header-links
  [:div#header-links
    "[ "
    [:a {:href "/"} "Home"]
    " | "
    [:a {:href "/till/new"} "Configure a new till"]
    " | "
    [:a {:href "/order/new"} "Start a new order"]
    " ]"])

(defn home-page
  []
  (hic-p/html5
    (gen-page-head "Home")
    header-links
    [:h1 "Home"]
    [:p "Welcome to Till-Clj, a Clojure Webapp built on Compojure for handling orders and receipts for restaurants!"]))

(defn add-till-page
  []
  (hic-p/html5
    (gen-page-head "Add a till")
    header-links
    [:h1 "Configure your new till"]
    [:form {:action "/till/create" :method "POST"}
     [:p "Shop Name: " [:input {:type "text" :name "shop-name"}]]
     [:p "Address: " [:input {:type "text" :name "address"}]]
     [:p "Phone: " [:input {:type "text" :name "phone"}]]
     [:p "Menu: " [:input {:type "text" :name "menu"}]]
     [:p [:input {:type "submit" :value "Create Till"}]]]))

(defn add-till
  [params]
  (let [[shop-name address phone menu]
       (vals params)]
    (db/add-till-to-db shop-name
                      address
                      phone
                      menu)))

(defn add-order-page
  []
  (hic-p/html5
    (gen-page-head "Add a new order")
    [:h1 "Place a new order"]
    [:form {:action "/order/new" :method "POST"}
     [:p "Server Name: " [:input {:type "text" :name "server_name" :placeholder "Enter your name here"}]]
     [:ul
      [:li "Menu Item 1: "
       [:input {:type "text" :name "quantity" :placeholder "Order quantity"}]]
      [:li "Menu Item 2: "
       [:input {:type "text" :name "quantity" :placeholder "Order quantity"}]]]
     [:p [:input {:type "submit" :value "Place order!"}]]]))

