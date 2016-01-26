(ns till-clj.db
  (:require [clojure.java.jdbc :as sql]))

(def db-spec {:classname "org.h2.Driver"
              :subprotocol "h2"
              :user "till-clj-ring"
              :subname "~/till-clj"
              :password ""})

;; (sql/with-db-connection [db-con db-spec]
;;   (let [rows (db-con ["select * from table where id = ?" 1])]
;;     (sql/insert! db-con :table (dissoc (first rows) :id))))

;; (sql/db-do-commands db-spec
;;                     (sql/create-table-ddl :tills
;;                                           [:shop-name "varchar(32)"]
;;                                           [:address   "varchar(32)"]
;;                                           [:phone     "varchar(32)"]
;;                                           [:menu      "varchar(32)"]))

;; (sql/db-do-commands db-spec "CREATE INDEX name_ix ON fruit ( name  )")

(defn new-id
  [table-name]
  (sql/with-db-connection [db-con db-spec]
    (let [results (sql/query db-con [(str "SELECT * FROM " table-name)]
                             :result-set-fn (fn [result-set]
                                              (count result-set)))]
      (+ results 1))))

(defn add-till-to-db
  [shop-name address phone menu]
  (sql/with-db-connection [db-con db-spec]
    (let [new-till-id (new-id "tills")
          results (sql/insert! db-con :tills {
                                    :id        new-till-id
                                    :shop_name shop-name
                                    :address   address
                                    :phone     phone
                                    :menu_id   menu})])))

  ;; (let [results (sql/insert! db-spec :tills
  ;;                                   {:shop-name shop-name
  ;;                                    :address   address
  ;;                                    :phone     phone
  ;;                                    :menu      menu})]
  ;;   (assert (= (count results) 1))
    ;; (first (vals results))))

(defn get-till-by-name
  [till-id]
  (sql/with-db-connection [db-con db-spec]
    (let [results (sql/query db-con ["SELECT * FROM tills WHERE id = ?" till-id])]
      (assert (= (count results) 1))
      (first results))))

;; (defn get-till-by-name
;;   [till-id]
;;   (let [results (sql/with-db-connection [db-con db-spec]
;;                   (sql/with-query-results res
;;                     ["select * from tills where id = ?" till-id]
;;                     (doall res)))]
;;     (assert (= (count results) 1))
;;     (first results)))

(defn get-all-tills
  []
  (sql/with-db-connection [db-con db-spec]
    (let [results (sql/query db-con ["SELECT * FROM tills"])]
      results)))
  ;; (let [results (sql/with-connection db-spec
  ;;                 (sql/with-query-results res
  ;;                   ["select * from tills"]
  ;;                   (doall res)))]
  ;;   results))
