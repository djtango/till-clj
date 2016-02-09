(ns till-clj.views
  (:require [till-clj.views.index :as index]
            [till-clj.views.till :as till]
            [till-clj.views.order :as order]))

(defn home-page
  []
  (index/home-page))

(defn add-till-page
  []
  (till/add-till-page))

(defn add-menu-page
  [params]
  (till/add-menu-page params))

(defn add-till-menu-items
  [params]
  (till/add-till-menu-items params))

(defn get-till-page
  []
  (till/get-till-page))

(defn update-till
  [params]
  (till/update-till params))

(defn menu-page
  [till-id]
  (till/menu-page till-id))

(defn edit-till-page
  [till-id]
  (till/edit-till-page till-id))

(defn add-order-page
  ([]
   (order/add-order-page))
  ([till-id]
   (order/add-order-page till-id)))

(defn order-page
  ([order-id]
  (order/order-page order-id)))

(defn create-order
  [params]
  (order/create-order params))
(defn print-receipt
  [order-id]
  (order/print-receipt order-id))
