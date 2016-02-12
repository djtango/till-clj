# till_clj

This is a web-based Clojure program for handling a till ordering system, and this project was chosen to learn web development in a functional language.

###Technologies used in this project:
______
*Language:* | Clojure
*Routing:* | Compojure
*Server:* | Ring
*Package Manager:* | Leiningen
*Testing:* | Speclj
*HTML DDL:* | Hiccup
*Database interface:* | java.jdbc
*Database:* | H2(SQL)

###Core User-stories:

```
As a restaurant chain owner
So I can maintain a central system for all my tills
I would like to be able to configure a new till for my restaurant

As a restaurant chain owner
So I can have flexibility across my restaurants
I would like each till and menu system to be individually configured

As a restaurant waiter
So I can easily place a customer order
I would like an interface to place orders at the restaurant

As a restaurant chain owner
So I can keep track of sales
I would like to have sales tracked by each till

As a restaurant manager
So I can measure staff performance
I would like to have sales tracked by each server

As a restaurant manager
So I can measure menu item performance
I would like to have sales tracked by each individual menu item

As a restaurant waiter
So I can complete a customer transaction
I would like to be able to print a receipt
```

###Design/Discussion:
As I have been reading SICP and Clojure was chosen as the language for its growing popularity, its tasty lisp syntax, immutable data structures and sizeable collection of libraries.
Of the testing suites available, Speclj had the greatest similarity to RSpec and was chosen for the descriptive style that is encouraged by the framework.
Leiningen, Ring and Compojure were identified as the quickest means to getting a web app running and I chose to use Hiccup to experience dynamic HTML generation.
With databasing, I opted to use the well documented java.jdbc and H2 (which it natively supports). I opted to not use SQL wrappers as I saw this as an opportunity to deepen my understanding of relational databasing and also develop my own abstractions.

###TODO/Next steps:
Add page to view all orders for a given till
Add functionality for editing an order
Introduce Clojurescript to improve form UX


## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein ring server

Then navigate to the URL:
```
localhost:3000/
```
## License

Copyright © 2016 FIXME
