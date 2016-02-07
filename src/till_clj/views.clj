(ns till-clj.views
  (:require [clojure.string :as str]
            [hiccup.page :as hic-p]
            [till-clj.db :as db]
            [till-clj.totals :as t]))

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
    " | "
    [:a {:href "/till/get"} "Manage your till"]
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

(defn get-till-page
  []
  (hic-p/html5
    (gen-page-head "Enter your till ID")
    header-links
    [:h1 "Manage your till:"]
    [:p "Please enter your till id:"
     [:form {:action "/till/get" :method "POST"}
      [:input {:type "text" :name "till_id" :placeholder "till-id"}]
      [:input {:type "submit" :value "Submit"}]]]))

(defn new-till-form
  [shop-name address phone path]
  [:form {:action path :method "POST"}
   [:p "Shop Name: " [:input {:type "text" :name "shop-name" :value shop-name :placeholder "your restaurant"}]]
   [:p "Address: " [:input {:type "text" :name "address" :value address :placeholder "restaurant address"}]]
   [:p "Phone: " [:input {:type "text" :name "phone" :value phone :placeholder "0123456789"}]]
   [:p "Number of menu items:" [:input {:type "number" :name "num_menu_items" :placeholder "number of menu items" :value 5}]]
   [:p [:input {:type "submit" :value "Save and continue"}]]])

(defn gen-form-rows
  [form input-num-rows]
  (let [num-rows (t/str->num input-num-rows)]
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
    (new-till-form nil nil nil "/till/menu/new")))

(defn add-till-menu-items
  [params]
  (db/add-till-menu-items params))

(defn edit-till-page
  [till-id]
  (let [till-data (db/get-till-menu-items till-id)
        first-row (first till-data)
        shop-name (:shop_name first-row)
        address   (:address first-row)
        phone     (:phone first-row)]
    (hic-p/html5
      (gen-page-head "Edit your till")
      header-links
      [:h1 "Edit your till"]
      [:form {:action "/till/update" :method "POST"}
       [:input {:type "hidden" :name "till_id" :value till-id}]
       [:p "Shop Name: " [:input {:type "text" :name "shop-name" :value shop-name :placeholder "your restaurant"}]]
       [:p "Address: " [:input {:type "text" :name "address" :value address :placeholder "restaurant address"}]]
       [:p "Phone: " [:input {:type "text" :name "phone" :value phone :placeholder "0123456789"}]]
       [:p [:input {:type "submit" :value "Submit"}]]])))

(defn update-till
  [params]
  (let [[till-id shop-name address phone]
        (vals params)]
    (db/update-till till-id
                    shop-name
                    address
                    phone)))

(defn gen-menu-rows
  [menu-rows till-data & [extra-html]]
  (let [current-row (first till-data)]
    (if (= till-data '())
     menu-rows
     (gen-menu-rows (conj menu-rows [:tr [:td (str (current-row :name))
                                          [:input {:type "hidden" :name "menu_item_id" :value (str (current-row :id_2))}]
                                          [:input {:type "hidden" :name "menu_item_price" :value (current-row :price)}]]
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
       (gen-menu-rows [:table] till-data)]
      [:p [:a {:href (str "/till/edit/" till-id)} "Edit your till"]])))

(defn add-order-page
  ([]
   (hic-p/html5
     (gen-page-head "Add a new order")
     [:h1 "Place a new order"]
     [:form {:action "/till/menu/get/order" :method "POST"}
      [:p "Please enter your till id:" [:input {:type "text" :name "till_id" :placeholder "till id"}]]
      [:p [:input {:type "submit" :value "Submit"}]]]))
  ([till-id]
   (let
     [till-data (db/get-till-menu-items till-id)]
     (hic-p/html5
      (gen-page-head "Add a new order")
      [:h1 "Place a new order"]
      [:form {:action "/order/create" :method "POST"}
       [:p "Server Name: " [:input {:type "text" :name "server_name" :placeholder "Enter your name here"}]]
       [:p
        (gen-menu-rows [:table] till-data [:td [:input {:type "number" :name "quantity" :value 0}]])]
       [:p [:input {:type "hidden" :name "till_id" :value till-id}]
        [:input {:type "submit" :value "Place your order!"}]]]))))

(defn create-order
  [params]
  (prn params)
  (db/add-order-menu-items params))

(defn gen-order-rows
  [order-rows order-data & [extra-html]]
  (let [current-row (first order-data)]
    (if (= order-data '())
      order-rows
      (gen-order-rows (conj order-rows [:tr [:td (str (current-row :name))]
                                        [:td (str (current-row :quantity))]
                                        (if extra-html
                                          (extra-html current-row))])
                      (rest order-data)
                      extra-html))))

(defn order-page
  [order-id]
  (let [order-data (db/get-order-menu-items order-id)
        first-row (first order-data)]
    (hic-p/html5
      (gen-page-head (first-row :shop_name))
      header-links
      [:h1 "Order " order-id ":"]
      [:p "Restaurant id: " (first-row :till_id)]
      [:p "Restaurant Name: " (first-row :shop_name)]
      [:p "Date of order: " (first-row :date)]
      [:p "Server Name: " (first-row :server)]
      [:p "Ordered items: "
       (gen-order-rows [:table] order-data)]
      [:p "Order total: £" (first-row :total)]
      [:a {:href (str order-id "/receipt")} "Print receipt"])))

(defn print-receipt
  [order-id]
  (let [order-data (t/assoc-subtotals (db/get-order-menu-items order-id))
        first-row (first order-data)]
    (hic-p/html5
      (gen-page-head (str "Order " order-id ": receipt"))
      header-links
      [:p (first-row :date)
       [:br] (first-row :shop_name)]
      [:p (first-row :address)
       [:br] "Phone: "(first-row :phone)]
      [:p (first-row :server)]
      [:p (gen-order-rows
            [:table]
            order-data
            (fn [row] (list [:td " x "]
                            [:td (:subtotal row)])))]
      [:p [:table [:tr [:td "Total:"] [:td "£"(first-row :total)]]]])))
