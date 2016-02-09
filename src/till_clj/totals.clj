(ns till-clj.totals)

(defn str->num
  [num-string]
  (if (string? num-string)
    (read-string num-string)
    num-string))

(defn find-zeroes
  [coll]
  (keep-indexed
    (fn [index value]
      (if (= 0 (str->num value))
        index))
    coll))

(defn drop-zero-rows
  [ref-coll coll]
  (let [zero-indexes (set (find-zeroes ref-coll))]
    (keep-indexed
      (fn [index value]
        (if-not (contains? zero-indexes
                           index)
          value))
      coll)))

(defn subtotal
  [input]
  (if (seq? input)
    (map subtotal input)
    (* (input (str->num :price))
     (input (str->num :quantity)))))

(defn assoc-subtotals
  [input]
  (if (seq? input)
    (map assoc-subtotals input)
    (assoc input :subtotal (subtotal input))))

(defn subtotal-params
  [params]
  (let [menu-item-prices (map str->num (params :menu_item_price))
        menu-item-ids (map str->num(params :menu_item_id))
        quantities (map str->num (params :quantity))]
    (into {}
          (map (fn [menu-item-price menu-item-id quantity]
                 (hash-map (keyword (str menu-item-id))
                           (* menu-item-price quantity)))
               menu-item-prices
               menu-item-ids
               quantities))))

(defn total
  ([params]
  (->> params
       subtotal-params
       vals
       (reduce +)))
  ([menu-item-prices quantities]
   (reduce + (map #(* (str->num %1)
                      (str->num %2))
                  menu-item-prices
                  quantities))))
