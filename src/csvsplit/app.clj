(ns csvsplit.app
  (:use [csvsplit.util])
  (:require [clojure-csv.core    :as csv]
            [csvsplit.file       :as file]))

(defn- split-csv
  [data cnt]
  (let [csv-dat    (csv/parse-csv data)
        header     (nth csv-dat 0)
        all-rows   (rest csv-dat)
        row-groups (partition cnt cnt [] all-rows)
        csv-segs   (map #(cons header %) row-groups)]
    csv-segs))

(defn- generate-csv-file
  [output-dir file-name csv-group]
  (let [full-path (file/join-paths output-dir file-name)
        csv-str (csv/write-csv csv-group)]
    (spit full-path csv-str)
    nil))

(defn- generate-csv-files
  [output-dir base-name csv-groups]
  (let [full-dir  (file/join-paths output-dir base-name)
        segs-idxd (map vector (range 1 (+ 1 (count csv-groups))) csv-groups)]
    (file/init-dir full-dir)
    (doall (backwards-map
              segs-idxd
              (fn [[idx csv-group]]
                (let [f-name (str base-name "." idx ".csv")]
                  (generate-csv-file full-dir f-name csv-group)))))
    nil))

(defn run
  [path cnt]
  (let [abs-path  (file/get-absolute-path path)
        out-dir   (file/get-parent-dir abs-path)
        base-name (file/get-file-name abs-path)
        raw-dat   (slurp abs-path)
        csv-segs  (split-csv raw-dat cnt)]
    (generate-csv-files out-dir base-name csv-segs)))