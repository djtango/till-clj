(ns till-clj.db.helpers
  (:require [clojure.java.jdbc :as sql]))

(def db-spec {:classname "org.h2.Driver"
              :subprotocol "h2"
              :user "till-clj-ring"
              :subname "~/till-clj"
              :password ""})

(defn insert-row
  [table & {:keys [column-name column-value] :as data-insert}]
  (prn (str "insert-row - table: " table " data-insert: " data-insert))
  (sql/with-db-connection [db-con db-spec]
    (sql/insert! db-con
                 table
                 data-insert)))

(defn insert-rows
  [table keys-vec & collections]
  (let [table-rows (map #(zipmap keys-vec %) (apply map list collections))]
    (prn (str "insert-rows - table: " table " keys-vec: " keys-vec " collections: " collections))
    (sql/with-db-connection [db-con db-spec]
      (apply (partial sql/insert! db-con table) table-rows))))

(defn inserted-ids
  [db-insert-output]
  (->> db-insert-output
       doall
       flatten
       (map (keyword "scope_identity()"))))

(defn time-now
  []
  (java.util.Date.))
