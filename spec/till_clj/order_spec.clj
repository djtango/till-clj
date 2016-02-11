(ns till-clj.order-spec
  (:require [speclj.core :refer :all]
            [ring.mock.request :as mock]
            [till-clj.handler :refer :all]
            [speclj.spec-helper :refer :all]
            [till-clj.db.init :as dbi]))

(describe "Preparing an order"
  (with-all response (GET "/order/new"))
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
  (with-all! test-data {:shop_name "Subway"
                        :address   "76 Joel Street"
                        :phone     "01923 835890"
                        :menu_item_names ["Chicken & Bacon Ranch Melt"
                                          "SUBWAY MELT"
                                          "Steak & Cheese"
                                          "Veggie Patty"
                                          "Meatball Marinara"
                                          "Italian B.M.T."]
                        :menu_item_prices [2.99
                                           3.15
                                           3.55
                                           3.99
                                           3.99
                                           4.20]})
  (before-all (dbi/db-migrate))
  (before-all (till-clj.db/add-till-menu-items! @test-data))
  (after-all (dbi/db-migrate))
  (with-all response (GET "/till/menu/1/order/new"))

  (it "testing DB to return results"
      (let [results (till-clj.db/get-till-menu-items 1)]
        (should= 1
                 (:id (first results)))))
  (it "User should be prompted to enter their name"
      (should-contain "name=\"server_name"
                      (:body @response)))
  (it "Form has a submit field"
      (should-contain "type=\"submit\""
                      (:body @response)))
  (it "Form should post to /order/create"
      (should-contain "action=\"/order/create\" method=\"POST\""
                      (:body @response)))
  (context "Testing DB data retrieval"
    (it "Menu has correct number of rows"
        (should= 6
                (match-count #"\<tr\>"
                             (:body @response))))
    (it "Menu has right number of quantity fields"
        (should= 6
                (match-count #"name=\"quantity\""
                             (:body @response))))
    (it "All quantity fields are number types"
        (should= (match-count #"name=\"quantity\""
                              (:body @response))
                 (match-count #"name=\"quantity\"\stype=\"number\""
                              (:body @response))))
    (it "Menu contents correctly retrieved and prices correctly assigned"
        (should= 1 (match-count
                     #"Chicken\s&\sBacon\sRanch\sMelt.*2\.99.*SUBWAY\sMELT.*3\.15.*Steak\s&\sCheese.*3\.55.*Veggie\sPatty.*3\.99.*Meatball\sMarinara.*3\.99.*Italian\sB\.M\.T\..*4\.20"
                                (:body @response))))))

(describe
  "Confirming and viewing the order"
  (with-all! test-data {:shop_name "Subway"
                        :address   "76 Joel Street"
                        :phone     "01923 835890"
                        :menu_item_names ["Chicken & Bacon Ranch Melt"
                                          "SUBWAY MELT"
                                          "Steak & Cheese"
                                          "Veggie Patty"
                                          "Meatball Marinara"
                                          "Italian B.M.T."]
                        :menu_item_prices [2.99
                                           3.15
                                           3.55
                                           3.99
                                           3.99
                                           4.20]})
  (with-all! params {:server_name "Deon"
                     :menu_item_id    [1    2    3    4    5    6]
                     :menu_item_price [2.99 3.15 3.55 3.99 3.99 4.20]
                     :quantity        [0    1    2    0    3    0]
                     :till_id         1})
  (before-all till-clj.db.init/db-migrate)
  (before-all (till-clj.db/add-till-menu-items! @test-data))
  (with-all response (POST "/order/create"
                           @params))
  (it "POST of the order params to /order/create should launch a redirect"
      (should= 302
               (:status @response)))
  (it "Page displays the restaurant id"
      (should= "http://localhost/order/1"
               (-> @response
                   :headers
                   (get "Location"))))
  (context "Viewing the order"
    (with-all response (GET "/order/1"))
    (it "Page displays the restaurant name"
        (should-contain "Subway"
                        (:body @response)))
    (it "Page displays date of order"
        (should-contain "2016-02-11"
                        (:body @response)))
    (it "Page displays server's name"
        (should-contain "Deon"
                        (:body @response)))
    (it "Page displays list of ordered items"
        (should= 3
                 (match-count #"\<tr\>"
                              (:body @response))))
    (it "Page displays the quantities of ordered items"
        (should= 1
                 (match-count #"SUBWAY\sMELT.*1.*Steak\s&\sCheese.*2.*Meatball\sMarinara.*3"
                              (:body @response))))
    (it "Page displays the order total"
        (should-contain "£22.22"
                        (:body @response)))
    (it "Page links to printing a receipt for the order"
        (should-contain "href=\"1/receipt\""
                        (:body @response)))))

(run-specs)
