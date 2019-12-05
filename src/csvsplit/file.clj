(ns csvsplit.file)

(defn get-parent-dir
  [path]
  (let [f (java.io.File. path)
        p (.getParentFile f)]
    (.getAbsolutePath p)))

(defn- drop-extension
  [filename]
  (let [segs   (clojure.string/split filename #"\.")
        n-segs (drop-last segs)]
    (clojure.string/join "." n-segs)))

(defn get-file-name
  [path]
  (let [f (java.io.File. path)
        n (.getName f)]
    (drop-extension n)))

(defn get-absolute-path
  [path]
  (let [f (java.io.File. path)]
    (.getAbsolutePath f)))

(defn join-paths
  [par f]
  (let [pth (java.nio.file.Paths/get par (into-array [f]))]
    (.toString pth)))

(defn init-dir
  [path]
  (let [f (java.io.File. path)]
    (if (not (.exists f))
      (do (.mkdir f)
          nil)
      nil)))