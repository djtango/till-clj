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

(def menu-item-line
 [:li [:input {:type "text" :name "menu_item_name" :placeholder "name"}]
  [:input {:type "text" :name "menu_item_price" :placeholder "price"}]])

(defn home-page
  []
  (hic-p/html5
    (gen-page-head "Home")
    header-links
    [:h1 "Home"]
    [:p "Welcome to Till-Clj, a Clojure Webapp built on Compojure for handling orders and receipts for restaurants!"]))

(defn new-till-form
  [shop-name address phone menu-items]
  [:form {:action "/till/menu/new" :method "POST"}
   [:p "Shop Name: " [:input {:type "text" :name "shop-name" :value shop-name :placeholder "your restaurant"}]]
   [:p "Address: " [:input {:type "text" :name "address" :value address :placeholder "restaurant address"}]]
   [:p "Phone: " [:input {:type "text" :name "phone" :value phone :placeholder "0123456789"}]]
   [:p "Number of menu items:" [:input {:type "number" :name "num_menu_items" :placeholder "number of menu items" :value 5}]]
   [:p [:input {:type "submit" :value "Save and continue"}]]])

(defn str->num
  [num-string]
  (if (string? num-string)
    (read-string num-string)
    num-string))

(defn gen-form-rows
  [form input-num-rows]
  (let [num-rows (str->num input-num-rows)]
    (if (<= num-rows 0)
     form
     (gen-form-rows
       (conj form menu-item-line)
       (- num-rows 1)))))

(defn menu-item-rows
  [num-rows]
  (gen-form-rows
    [:ul menu-item-line]
    num-rows))

(defn add-menu-page
  [params]
  (let [[shop-name address phone num-rows]
        (vals params)]
    (hic-p/html5
      (gen-page-head "Add your menu")
      header-links
      [:h1 "Configure your menu"]
      [:form {:action "/till/create" :method "POST"}
       [:input {:type "hidden" :name "menu_name" :value shop-name}]
       [:input {:type "hidden" :name "address" :value address}]
       [:input {:type "hidden" :name "phone" :value phone}]
       (menu-item-rows num-rows)
       [:input {:type "submit" :value "Add menu"}]])))

(defn add-till-page
  []
  (hic-p/html5
    (gen-page-head "Add a till")
    header-links
    [:h1 "Configure your new till"]
    (new-till-form nil nil nil nil)))

(defn add-till-menu-items
  [params]
  (db/add-till-menu-items params))

(defn gen-menu-rows
  [menu-rows till-data & [extra-html]]
  (let [current-row (first till-data)]
    (if (= till-data '())
     menu-rows
     (gen-menu-rows (conj menu-rows [:tr [:td (str (current-row :name)) [:input {:type "hidden" :name "menu_item_id" :value (str (current-row :id_2))}]]
                                     [:td (str (current-row :price))]
                                     (if extra-html
                                       extra-html)])
                    (rest till-data)
                    extra-html))))

(defn menu-page
  [till-id]
  (let [till-data (db/get-till-menu-items till-id)
        first-till (first till-data)]
    (hic-p/html5
      (gen-page-head (first-till :shop_name))
      header-links
      [:h1 (first-till :shop_name) ":"]
      [:p "Restaurant id: " (str (first-till :id))]
      [:p "Address: " (first-till :address)]
      [:p "Phone: " (first-till :phone)]
      [:p "Menu: "
       (gen-menu-rows [:table] till-data)])))

(defn add-order-page
  ([]
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
  ([till-id]
   (let
     [till-data (db/get-till-menu-items till-id)]
     (hic-p/html5
      (gen-page-head "Add a new order")
      [:h1 "Place a new order"]
      [:form {:action "/order/create" :method "POST"}
       [:p "Server Name: " [:input {:type "text" :name "server_name" :placeholder "Enter your name here"}]]
       [:p
        (gen-menu-rows [:table] till-data [:td [:input {:type "number" :value 0}]])]
       [:p [:input {:type "submit" :value "Place your order!"}]]]))))

(defn create-order
  [params]
  (prn (str "create-order: " params)))
