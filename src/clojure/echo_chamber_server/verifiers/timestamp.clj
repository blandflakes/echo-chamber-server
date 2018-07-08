(ns echo-chamber-server.verifiers.timestamp
  (:require [clj-time.core :as t]
            [clj-time.format :as time-format]))

(defn- absolute-interval
  "Returns the interval between the two datetime instances, regardless of which came first."
  [one another]
  (if (t/before? one another)
    (t/interval one another)
    (t/interval another one)))

(defn within-tolerance?
  "Returns true if the duration between the time the request was made and now is within the provided tolerance."
  [request-datetime tolerance-seconds now-datetime]
  (let [interval (absolute-interval request-datetime now-datetime)
        diff (t/in-seconds interval)]
    (< diff tolerance-seconds)))

(defn verifier
  "Returns a verifier than can be applied to a request. Default tolerance is 150 seconds."
  ([] (verifier 150))
  ([tolerance-in-seconds]
   (let [datetime-formatter (time-format/formatters :date-time-no-ms)]
     (fn [_http-exchange request-envelope _request-bytes]
       (let [timestamp (get-in request-envelope ["request" "timestamp"])
             datetime-of-request (time-format/parse datetime-formatter timestamp)
             now (t/now)]
         (if (not (within-tolerance? datetime-of-request tolerance-in-seconds now))
           (throw (SecurityException. "Date of request out of tolerance."))))))))