(ns csvsplit.validation)

(defn- invalid-length?
  [args]
  (not= 2 (count args)))

(defn- invalid-path?
  [args]
  (let [p (nth args 0)
        f (java.io.File. p)]
    (cond
      (= false (.exists f)) true
      (= false (.isFile f)) true
      :else                 false)))

(defn- invalid-count?
  [args]
  (let [len-str (nth args 1)
        len     (Integer. len-str)]
    (<= len 0)))

(defn validate
  [args]
  (cond
    (invalid-length? args) :invalid-usage
    (invalid-path? args)   :invalid-path
    (invalid-count? args)  :invalid-count
    :else                  nil))