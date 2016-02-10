(ns till-clj.order-spec
  (:require [speclj.core :refer :all]
            [ring.mock.request :as mock]
            [till-clj.handler :refer :all]
            [speclj.spec-helper :refer :all]))

(describe "Preparing an order"
  (with-all response (GET "/order/new"))
  (it "GET to /order/new responds with 200"
    (should= 200
             (:status @response)))
  (it "User is prompted with form to enter till id"
    (should-contain "name=\"till_id\""
                    (:body @response)))
  (it "Page has a submit field"
    (should-contain "type=\"submit\""
                    (:body @response)))
  (it "Submit POSTs to ../till/menu/get/order"
    (should-contain "action=\"../till/menu/get/order\""
                    (:body @response))))

(describe "Submitting a till-id to /till/menu/get/order"
  (with-all! params {:till_id 1})
  (with-all response (POST "/till/menu/get/order" @params))
  (it "POST request redirects user to appropriate ordering page"
    (should= 302
             (:status @response)))
  (it "redirected page corresponds to entered till id"
    (should= "http://localhost/till/menu/1/order/new"
             (-> @response
                 :headers
                 (get "Location")))))

(describe "Placing an order"
  (with-all! ))
