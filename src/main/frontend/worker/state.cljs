(ns frontend.worker.state
  "State hub for worker"
  (:require [logseq.common.util :as common-util]))

(defonce *state (atom {:db/latest-transact-time {}
                       :worker/context {}

                       ;; FIXME load graph config when fetch-initial-data
                       :config {}
                       :repo/current nil}))

(defonce *sqlite (atom nil))
;; repo -> {:db conn :search conn}
(defonce *sqlite-conns (atom nil))
;; repo -> conn
(defonce *datascript-conns (atom nil))
;; repo -> pool
(defonce *opfs-pools (atom nil))

(defn get-sqlite-conn
  [repo & {:keys [search?]
           :or {search? false}
           :as _opts}]
  (let [k (if search? :search :db)]
    (get-in @*sqlite-conns [repo k])))

(defn get-datascript-conn
  [repo]
  (get @*datascript-conns repo))

(defn get-opfs-pool
  [repo]
  (get @*opfs-pools repo))

(defn tx-idle?
  [repo & {:keys [diff]
           :or {diff 1000}}]
  (when repo
    (let [last-input-time (get-in @*state [:db/latest-transact-time repo])]
      (or
       (nil? last-input-time)

       (let [now (common-util/time-ms)]
         (>= (- now last-input-time) diff))))))

(defn set-db-latest-tx-time!
  [repo]
  (swap! *state assoc-in [:db/latest-transact-time repo] (common-util/time-ms)))

(defn get-context
  []
  (:worker/context @*state))

(defn set-context!
  [context]
  (swap! *state assoc :worker/context context))

(defn get-config
  [repo]
  (get-in @*state [:config repo]))

(defn get-current-repo
  []
  (:repo/current @*state))
