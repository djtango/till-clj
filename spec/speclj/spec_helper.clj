(ns speclj.spec-helper
  (:require [speclj.core :refer :all]
            [ring.mock.request :as mock]
            [till-clj.handler :refer :all]))

(defn GET [path]
  (app (mock/request :get path)))

(defn POST [path params]
  (app (mock/request :post path params)))

(defn match-count
  [re s]
  (->> s
       (re-seq re)
       count))
