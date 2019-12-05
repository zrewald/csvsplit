(ns csvsplit.core
  (:require [csvsplit.validation :as validation]
            [csvsplit.app        :as app])
  (:gen-class))

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
      (let [err (validation/validate args)]
        (if (= nil err)
          (let [pth (nth args 0)
                cnt (Integer. (nth args 1))]
            (app/run pth cnt))
          (show-error err)))
      (show-usage))
    (catch Exception e (println e) (show-error :unknown))))

