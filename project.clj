(defproject till_clj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [compojure "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [org.clojure/java.jdbc "0.4.2"]
                 [com.h2database/h2 "1.4.191"]
                 [ring/ring-anti-forgery "1.0.0"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler till-clj.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
