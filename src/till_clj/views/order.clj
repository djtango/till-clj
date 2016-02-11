(ns till-clj.views.order
  (:require [clojure.string :as str]
            [hiccup.page :as hic-p]
            [till-clj.db :as db]
            [till-clj.totals :as t]
            [till-clj.views.helpers :as vh]))

(defn add-order-page
  ([]
   (hic-p/html5
     (vh/title-banner "Add a new order")
     [:h1 "Place a new order"]
     [:form {:action "../till/menu/get/order" :method "POST"}
      [:p "Please enter your till id:" [:input {:type "text" :name "till_id" :placeholder "till id"}]]
      [:p [:input {:type "submit" :value "Submit"}]]]))

  ([till-id]
   (let
     [till-data (db/get-till-menu-items till-id)]
     (hic-p/html5
      (vh/title-banner "Add a new order")
      [:h1 "Place a new order"]
      [:form {:action "/order/create" :method "POST"}
       [:p "Server Name: " [:input {:type "text" :name "server_name" :placeholder "Enter your name here"}]]
       [:p
        (vh/gen-menu-rows [:table] till-data [:td [:input {:type "number" :name "quantity" :value 0}]])]
       [:p [:input {:type "hidden" :name "till_id" :value till-id}]
        [:input {:type "submit" :value "Place your order!"}]]]))))

(defn create-order
  [params]
  (prn params)
  (db/add-order-menu-items! params))

(defn order-page
  [order-id]
  (let [order-data (db/get-order-menu-items order-id)
        first-row (first order-data)]
    (hic-p/html5
      (vh/title-banner (:shop_name first-row))
      [:h1 "Order " order-id ":"]
      [:p "Restaurant id: " (:till_id first-row)]
      [:p "Restaurant Name: " (:shop_name first-row)]
      [:p "Date of order: " (:date first-row)]
      [:p "Server Name: " (:server first-row)]
      [:p "Ordered items: "
       (vh/gen-order-rows [:table] order-data)]
      [:p "Order total: £" (:total first-row)]
      [:a {:href (str order-id "/receipt")} "Print receipt"])))

(defn print-receipt
  [order-id]
  (let [order-data (t/assoc-subtotals (db/get-order-menu-items order-id))
        first-row (first order-data)]
    (hic-p/html5
      (vh/title-banner (str "Order " order-id ": receipt"))
      [:p (:date first-row)
       [:br] (:shop_name first-row)]
      [:p (:address first-row)
       [:br] "Phone: "(:phone first-row)]
      [:p (:server first-row)]
      [:p (vh/gen-order-rows
            [:table]
            order-data
            (fn [row] (list [:td " x "]
                            [:td (:subtotal row)])))]
      [:p [:table [:tr [:td "Total:"] [:td "£"(:total first-row)]]]])))
