(ns till-clj.index-spec
  (:require [speclj.core :refer :all]
            [till-clj.handler :refer :all]
            [speclj.spec-helper :refer :all]
            ))

(describe "Landing Page"
  (with-all response (GET "/"))
  (it "GET to / returns 200"
      (should= 200
               (:status @response)))
  (it "Displays landing page"
    (should-contain "Welcome to Till-Clj"
                    (:body @response))))

(run-specs)
