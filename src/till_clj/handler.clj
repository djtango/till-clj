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
        (views/add-till params)
        (redirect "/"))
  (GET "/order/new"
       []
       (views/add-order-page))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-defaults (assoc site-defaults :security false))))
