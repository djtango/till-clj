(ns till-clj.handler
  (:use ring.util.response)
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [till-clj.views :as views]))

(defroutes app-routes
  (GET "/"
       []
       (views/home-page))

  (GET "/till/new"
       []
       (views/add-till-page))
  (POST "/till/menu/new"
        {params :params}
        (views/add-menu-page params))
  (POST "/till/create"
        {params :params}
        (prn params)
        (let [till-id (views/add-till-menu-items params)]
          (redirect (str "/till/menu/" till-id))))
  (GET "/till/get"
       []
       (views/get-till-page))
  (POST "/till/update"
        {params :params}
        (let [till-id (:till_id params)]
          (views/update-till params)
          (redirect (str "/till/menu/" till-id))))
  (GET "/till/menu/:till-id"
       [till-id]
       (views/menu-page till-id))
  (GET "/till/edit/:till-id"
       [till-id]
       (views/edit-till-page till-id))
  (POST "/till/get"
        {params :params}
        (prn params)
        (let [till-id (:till_id params)]
          (redirect (str "/till/menu/" till-id))))
  (POST "/till/menu/get/order"
        {params :params}
        (let [till-id (:till_id params)]
          (redirect (str "/till/menu/" till-id "/order/new"))))

  (GET "/till/menu/:till-id/order/new"
       [till-id]
       (views/add-order-page till-id))
  (GET "/order/new"
       []
       (views/add-order-page))
  (POST "/order/create"
          {params :params}
          (let [order-id (views/create-order params)]
            (redirect (str "/order/" order-id))))
  (GET "/order/:order-id"
       [order-id]
       (views/order-page order-id))
  (GET "/order/:order-id/receipt"
       [order-id]
       (views/print-receipt order-id))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-defaults (assoc site-defaults :security false))))
