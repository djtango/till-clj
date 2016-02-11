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

;; (describe "Adding to the database"
;;   (xit "Should add the till to the db")
;;   (xit "Should add the menu items to the db")
;;   (xit "Should add the join rows")
;;   (xit "Should redirect the user to the correct page"))

(run-specs)
