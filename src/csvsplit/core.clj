(ns csvsplit.core
  (:require [clojure-csv.core :as csv])
  (:gen-class))

(defn- split-csv
  [data cnt]
  (let [csv-dat    (csv/parse-csv data)
        header     (nth csv-dat 0)
        all-rows   (rest csv-dat)
        row-groups (partition cnt cnt [] all-rows)
        csv-segs   (map #(cons header %) row-groups)]
    csv-segs))

(defn- drop-extension
  [filename]
  (let [segs   (clojure.string/split filename #"\.")
        n-segs (drop-last segs)]
    (clojure.string/join "." n-segs)))

(defn- get-output-dir
  [path]
  (let [f (java.io.File. path)
        p (.getParentFile f)]
    (.getAbsolutePath p)))

(defn- get-file-name
  [path]
  (let [f (java.io.File. path)
        n (.getName f)]
    (drop-extension n)))

(defn- backwards-map
  [items f]
  (map f items))

(defn- join-paths
  [par f]
  (let [pth (java.nio.file.Paths/get par (into-array [f]))]
    (.toString pth)))

(defn- generate-csv-file
  [output-dir file-name csv-group]
  (let [full-path (join-paths output-dir file-name)
        csv-str (csv/write-csv csv-group)]
    (spit full-path csv-str)
    nil))

(defn- init-dir
  [path]
  (let [f (java.io.File. path)]
    (if (not (.exists f))
      (do (.mkdir f)
          nil)
      nil)))

(defn- generate-csv-files
  [output-dir base-name csv-groups]
  (let [full-dir  (join-paths output-dir base-name)
        segs-idxd (map vector (range 1 (+ 1 (count csv-groups))) csv-groups)]
    (init-dir full-dir)
    (doall (backwards-map
             segs-idxd
             (fn [[idx csv-group]]
               (let [f-name (str base-name "." idx ".csv")]
                 (generate-csv-file full-dir f-name csv-group)))))
    nil))

(defn- get-absolute-path
  [path]
  (let [f (java.io.File. path)]
    (.getAbsolutePath f)))

(defn- run
  [path cnt]
  (let [abs-path  (get-absolute-path path)
        out-dir   (get-output-dir abs-path)
        base-name (get-file-name abs-path)
        raw-dat   (slurp abs-path)
        csv-segs  (split-csv raw-dat cnt)]
    (generate-csv-files out-dir base-name csv-segs)))

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

(defn- validate
  [args]
  (cond
    (invalid-length? args) :invalid-usage
    (invalid-path? args)   :invalid-path
    (invalid-count? args)  :invalid-count
    :else                  nil))

(defn- show-usage
  []
  (println (str "\n"
                "csvsplit - A tool for splitting big-ass CSV files.\n"
                "\n"
                "Usage: java -jar [path/to/]csvsplit.jar [input file path] [rows-per-file]\n")))

(defn- show-error
  [err]
  (let [err-str (case err
                  :invalid-usage "invalid usage.  Run java -jar [path/to/]csvsplit.jar for help."
                  :invalid-path  "path must be an existing CSV file."
                  :invalid-count "item count must be greater than 0."
                                 "unknown error")]
    (println (str "Error: " err-str))))

(defn -main
  [& args]
  (try
    (if (not= 0 (count args))
      (let [err (validate args)]
        (if (= nil err)
          (let [pth (nth args 0)
                cnt (Integer. (nth args 1))]
            (run pth cnt))
          (show-error err)))
      (show-usage))
    (catch Exception e (println e) (show-error :unknown))))

