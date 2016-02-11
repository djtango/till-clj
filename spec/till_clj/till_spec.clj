(ns till-clj.till-spec
  (:require [speclj.core :refer :all]
            [till-clj.handler :refer :all]
            [speclj.spec-helper :refer :all]))

(describe "Setting up a new till:"
  (with-all response (GET "/till/new"))
  (it "GET to /till/new returns 200"
    (should= 200
             (:status @response)))
  (it "Page has header"
    (should-contain "Configure your new till"
                    (:body @response)))
  (it "Displays a form with four inputs and a submit"
    (should= 5
             (match-count #"\<input" (:body @response)))))

(describe "Configuring the menu"
  (with-all! params {:shop_name       "Cafe"
                      :address        "123 SomeStreet"
                      :phone          "0123456789"
                      :num_menu_items 5})
  (with response (POST "/till/menu/new" @params))
  (it "POST to /till/menu/new returns 200"
      (should= 200
                (:status @response)))
  (it "Page has header"
      (should-contain "Configure your menu"
                      (:body @response)))
  (it "Generates menu form with 5 rows"
      (should= 5
                (match-count #"\<li\>" (:body @response))))
  (it "Count of menu_item_name and menu_item_price rows is equal"
      (should= (match-count #"menu_item_name" (:body @response))
               (match-count #"menu_item_price" (:body @response))))
  (it "Page has a submit button"
      (should-contain "type=\"submit\""
                      (:body @response)))
  (it "shop name is passed through"
      (should-contain "name=\"shop_name\""
                      (:body @response)))
  (it "Address is passed through"
      (should-contain "name=\"address\""
                      (:body @response)))
  (it "Phone number is passed through"
      (should-contain "name=\"phone\""
                      (:body @response)))
  (context "Varying number of menu rows"
    (with-all! params {:shop_name       "Cafe"
                        :address        "123 SomeStreet"
                        :phone          "0123456789"
                        :num_menu_items 10})
    (it "Number of rows changes with num_menu_items"
        (should= 10
                 (match-count #"\<li\>" (:body @response))))
    (it "Count of menu_item_name and menu_item_price rows equal"
        (should= (match-count #"menu_item_name" (:body @response))
                 (match-count #"menu_item_price" (:body @response))))))

(describe
  "Adding to the database"
  (before-all (till-clj.db.init/db-migrate))
  (after-all (till-clj.db.init/db-migrate))
  (with-all! params {:shop_name "Subway"
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
  (with-all response (POST "/till/create"
                           @params))
  (it "POST with params /till/create launches responds with a redirect"
      (should= 302
               (:status @response)))
  (it "redirect URL points to the created till"
      (should= "http://localhost/till/menu/1"
               (-> @response
                   :headers
                   (get "Location"))))
  (context "Redirected to till page:"
    (with response (GET "/till/menu/1"))
    (it "Page should display the till added to the db"
      (should= 1
        (match-count #"Subway.*76\sJoel\sStreet.*01923\s835890"
                     (:body @response))))
    (it "Page should display the menu items added to the db"
      (should= 1
               (match-count #"Chicken\s&\sBacon\sRanch\sMelt.*2\.99.*SUBWAY\sMELT.*3\.15.*Steak\s&\sCheese.*3\.55.*Veggie\sPatty.*3\.99.*Meatball\sMarinara.*3\.99.*Italian\sB\.M\.T\..*4\.20"
                            (:body @response))))))

(run-specs)
