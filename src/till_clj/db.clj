(ns till-clj.db
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

(defn create-join-table
  []
  (sql/create-table-ddl :till_menu_items
                        [:till_id       :int :not :null]
                        [:menu_item_id :int :not :null]))

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
    (create-tills-table)
    (create-menu-items-table)
    (create-join-table)
    (add-primary-key :till_menu_items :till_id :menu_item_id)
    (add-foreign-key :till_menu_items :till_id :tills :id)
    (add-foreign-key :till_menu_items :menu_item_id :menu_items :id)))

(defn new-id
  [table-name]
  (sql/with-db-connection [db-con db-spec]
    (let [results (sql/query db-con [(str "SELECT * FROM " table-name)]
                             :result-set-fn (fn [result-set]
                                              (count result-set)))]
      (+ results 1))))

(defn add-till-to-db
  [shop-name address phone]
  (sql/with-db-connection [db-con db-spec]
    (sql/insert! db-con :tills {
                                :shop_name shop-name
                                :address   address
                                :phone     phone})))

(defn add-menu-items-to-db
  [menu-item-names
   menu-item-prices]
  (->> (map #(sql/with-db-connection [db-con db-spec]
               (sql/insert! db-con
                            :menu_items {
                                         :name  %1
                                         :price %2
                                         }))
            menu-item-names
            menu-item-prices)
       flatten
       (map (keyword "scope_identity()"))))

(defn get-till-by-name
  [till-id]
  (sql/with-db-connection [db-con db-spec]
    (let [results (sql/query db-con ["SELECT * FROM tills WHERE id = ?" till-id])]
      (assert (= (count results) 1))
      (first results))))

(defn get-all-tills
  []
  (sql/with-db-connection [db-con db-spec]
    (let [results (sql/query db-con ["SELECT * FROM tills"])]
      results)))

(defn new-id-range
  [table-name new-rows]
  (let [first-id (new-id table-name)
        num-rows (count new-rows)]
    (range first-id (+ first-id num-rows))))

(defn map-ids
  [table-name new-rows]
  (map (fn [row-item new-id]
         (assoc row-item :id new-id))
       new-rows
       (new-id-range table-name new-rows)))

(defn add-menu-to-db
  [menu]
  (sql/with-db-connection [db-con db-spec]
    (let [menu-items-ids (map-ids "menu_items" menu)
          results (sql/insert! db-con :menu_items menu-items-ids)])))

(defn insert-row
  [table & {:keys [column-name column-value] :as data-insert}]
  (sql/with-db-connection [db-con db-spec]
    (sql/insert! db-con
                 table
                 data-insert)))

(defn inserted-ids
  [db-insert-output]
  (->> db-insert-output
       flatten
       (map (keyword "scope_identity()"))))

(defn create-till-menu-items
  [params]
  (let [[shop-name address phone menu-item-names menu-item-prices]
         (vals params)
        inserted-till (first (inserted-ids (insert-row :tills
                                                       :shop_name shop-name
                                                       :address   address
                                                       :phone     phone)))
        inserted-menu-items (inserted-ids (map #(insert-row :menu_items
                                                            :name  %1
                                                            :price %2)
                                               menu-item-names
                                               menu-item-prices))]
    (for [menu-item-id inserted-menu-items]
      (insert-row :till_menu_items
                  :till_id      inserted-till
                  :menu_item_id menu-item-id))))
