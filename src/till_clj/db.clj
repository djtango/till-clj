(ns till-clj.db
  (:require [clojure.java.jdbc :as sql]
            [till-clj.totals :as t]
            [till-clj.db.helpers :as h]))

(def db-spec {:classname "org.h2.Driver"
              :subprotocol "h2"
              :user "till-clj-ring"
              :subname "~/till-clj"
              :password ""})

(defn get-till-menu-items
  [till-id]
  (sql/with-db-connection [db-con db-spec]
    (let [results (sql/query db-con ["SELECT * FROM tills t INNER JOIN till_menu_items tmi on tmi.till_id = t.id INNER JOIN menu_items mi on mi.id = tmi.menu_item_id WHERE t.id = ?" till-id])]
      results)))

(defn get-all-tills
  []
  (sql/with-db-connection [db-con db-spec]
    (let [results (sql/query db-con ["SELECT * FROM tills"])]
      results)))

(defn update-till
  [till-id shop-name address phone]
  (sql/with-db-connection [db-con db-spec]
    (sql/update! db-con
                 :tills
                 {:shop_name  shop-name
                  :address    address
                  :phone      phone}
                 ["id = ?" till-id])))

(defn add-till-menu-items
  [params]
  (let [[shop-name address phone menu-item-names menu-item-prices]
         (vals params)
        inserted-till (first (h/inserted-ids (h/insert-row :tills
                                                           :shop_name shop-name
                                                           :address   address
                                                           :phone     phone)))
        inserted-menu-items (h/inserted-ids (h/insert-rows :menu_items
                                                           [:name :price]
                                                           menu-item-names
                                                           menu-item-prices))]
    (h/insert-rows :till_menu_items
                   [:till_id :menu_item_id]
                   (repeat (count inserted-menu-items) inserted-till)
                   inserted-menu-items)
    inserted-till))

(defn add-order-menu-items
  [params]
  (let [[server menu-item-ids menu-item-prices quantities till-id]
        (vals params)
        total (t/total menu-item-prices quantities)
        date-time (h/time-now)
        inserted-order (first (h/inserted-ids (h/insert-row :orders
                                                        :total   total
                                                        :date    date-time
                                                        :server  server
                                                        :till_id till-id)))
        drop-zero-rows (fn [coll] ((partial t/drop-zero-rows quantities) coll))]
    (h/insert-rows :order_menu_items
                   [:order_id :menu_item_id :quantity]
                   (repeat (count (drop-zero-rows menu-item-ids)) inserted-order)
                   (drop-zero-rows menu-item-ids)
                   (drop-zero-rows quantities))
    inserted-order))

(defn get-order-menu-items
  [order-id]
  (sql/with-db-connection [db-con db-spec]
    (let [results (sql/query db-con ["SELECT * FROM orders o INNER JOIN order_menu_items omi on omi.order_id = o.id INNER JOIN menu_items mi on mi.id = omi.menu_item_id INNER JOIN tills t on t.id = o.till_id WHERE o.id = ?" order-id])]
      results)))
