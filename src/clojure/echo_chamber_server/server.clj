(ns echo-chamber-server.server
  (:import (io.undertow.server HttpHandler HttpServerExchange RoutingHandler)
           (java.io ByteArrayOutputStream ByteArrayInputStream)
           (io.undertow Undertow)
           (io.undertow.util Methods Headers HeaderMap HttpString))
  (:require [cheshire.core :refer [generate-string parse-stream]]))

(defn #^:private slurp-bytes
  "Reads bytes into a bytearray from a stream."
  [stream]
  (with-open [out (ByteArrayOutputStream.)]
    (clojure.java.io/copy stream out)
    (.toByteArray out)))

(defn skill-handler [verifiers skill-fn]
  (reify HttpHandler
    (^void handleRequest [_this ^HttpServerExchange exchange]
      (condp = (.getRequestMethod exchange)
        Methods/POST (let [request-bytes (slurp-bytes (.getInputStream exchange))
                           request-envelope (with-open [input-stream (ByteArrayInputStream. bytes)]
                                              (parse-stream input-stream))]
                       (.put ^HeaderMap (.getResponseHeaders exchange) ^HttpString Headers/CONTENT_TYPE_STRING "application/json")
                       (doseq [verifier verifiers]
                         (verifier exchange request-envelope request-bytes))
                       (let [response (skill-fn request-envelope)
                             sender (.getResponseSender exchange)]
                         (.send sender ^String (generate-string response))))
        Methods/HEAD (.setStatusCode exchange 200)
        (.setStatusCode exchange 405)))))

(defn app-server [^String host ^Integer port & skills]
  (let [builder (Undertow/builder)]
    (.addHttpListener builder port host)
    (let [router (RoutingHandler.)]
      (doseq [skill skills]
        (.get router (get skill :route "/") (skill-handler (:verifiers skill) (:skill-fn skill))))
      (.setHandler builder router))
    (let [server (.build builder)]
      {:start (fn [] (.start server))
       :stop  (fn [] (.stop server))})))