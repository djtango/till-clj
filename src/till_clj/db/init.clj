(ns till-clj.db.init
  (:require [clojure.java.jdbc :as sql]
            [clojure.string :as s]))

(def db-spec {:classname "org.h2.Driver"
              :subprotocol "h2"
              :user "till-clj-ring"
              :subname "~/till-clj"
              :password ""})

(defn create-tills-table
  []
  (sql/create-table-ddl :tills
                        [:id        "IDENTITY" :primary :key]
                        [:shop_name "VARCHAR(32)"]
                        [:address   "VARCHAR(255)"]
                        [:phone     "VARCHAR(32)"]))

(defn create-menu-items-table
  []
  (sql/create-table-ddl :menu_items
                        [:id    "IDENTITY" :primary :key]
                        [:name  "VARCHAR(32)"]
                        [:price "DECIMAL(20,2)"]))

(defn create-till-menu-items-table
  []
  (sql/create-table-ddl :till_menu_items
                        [:till_id      :int :not :null]
                        [:menu_item_id :int :not :null]))

(defn create-orders-table
  []
  (sql/create-table-ddl :orders
                        [:id      "IDENTITY"      :primary :key]
                        [:total   "DECIMAL(20,2)" :not :null]
                        [:date    "DATE"          :not :null]
                        [:server  "VARCHAR(255)"  :not :null]
                        [:till_id :int            :not :null]))

(defn create-order-menu-items-table
  []
  (sql/create-table-ddl :order_menu_items
                        [:order_id     :int :not :null]
                        [:menu_item_id :int :not :null]
                        [:quantity     :int :not :null]))

(defn add-primary-key
  [table & primary-keys]
  (format "ALTER TABLE %s ADD PRIMARY KEY (%s)"
          (name table)
          (s/join ", " (map name primary-keys))))

(defn exists?
  [db-spec table]
  (try
    (do
      (->> (format "SELECT * FROM %s WHERE ROWNUM <= 1" (name table))
           (vector)
           (sql/query db-spec))
      true)
    (catch Throwable ex
      false)))

(defn add-foreign-key
  [table table-column ref-table ref-key]
  (format "ALTER TABLE %s ADD FOREIGN KEY (%s) REFERENCES %s (%s)"
          (name table)
          (name table-column)
          (name ref-table)
          (name ref-key)))

(defn drop-if-exists
  [db-spec table]
  (if (exists? db-spec table)
    (sql/drop-table-ddl table)))

(defn init-db
  []
  (sql/db-do-commands
    db-spec
    (drop-if-exists db-spec :tills)
    (drop-if-exists db-spec :menu_items)
    (drop-if-exists db-spec :till_menu_items)
    (drop-if-exists db-spec :orders)
    (drop-if-exists db-spec :order_menu_items)
    (create-tills-table)
    (create-menu-items-table)
    (create-till-menu-items-table)
    (create-orders-table)
    (create-order-menu-items-table)
    (add-primary-key :till_menu_items :till_id :menu_item_id)
    (add-foreign-key :till_menu_items :till_id :tills :id)
    (add-foreign-key :till_menu_items :menu_item_id :menu_items :id)
    (add-primary-key :order_menu_items :order_id :menu_item_id)
    (add-foreign-key :orders :till_id :tills :id)
    (add-foreign-key :order_menu_items :order_id :orders :id)
    (add-foreign-key :order_menu_items :menu_item_id :menu_items :id)))
