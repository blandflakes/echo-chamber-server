(ns echo-chamber-server.server
  (:import (io.undertow.server HttpHandler HttpServerExchange RoutingHandler)
           (java.io ByteArrayOutputStream ByteArrayInputStream)
           (io.undertow Undertow)
           (io.undertow.util Headers HeaderMap HttpString)
           (io.undertow.server.handlers BlockingHandler))
  (:require [cheshire.core :refer [generate-string parse-stream]]))

(defn #^:private slurp-bytes
  "Reads bytes into a bytearray from a stream."
  [stream]
  (with-open [out (ByteArrayOutputStream.)]
    (clojure.java.io/copy stream out)
    (.toByteArray out)))

(defn #^:private skill-handler [verifiers skill-fn]
  (BlockingHandler.
    (reify HttpHandler
      (^void handleRequest [_this ^HttpServerExchange exchange]
        (let [request-bytes (slurp-bytes (.getInputStream exchange))
              request-envelope (with-open [reader (clojure.java.io/reader
                                                    (ByteArrayInputStream. request-bytes))]
                                 (parse-stream reader))]
          (.put ^HeaderMap (.getResponseHeaders exchange) ^HttpString Headers/CONTENT_TYPE "application/json")
          (doseq [verifier verifiers]
            (verifier exchange request-envelope request-bytes))
          (let [response (skill-fn request-envelope)
                sender (.getResponseSender exchange)]
            (.send sender ^String (generate-string response))))))))

(defn app-server [^String host ^Integer port & skills]
  (let [builder (Undertow/builder)]
    (.addHttpListener builder port host)
    (let [router (RoutingHandler.)]
      (doseq [skill skills]
        (.post router (get skill :route "/") (skill-handler (:verifiers skill) (:skill-fn skill)))
        (.setHandler builder router))
      (let [server (.build builder)]
        {:start (fn [] (.start server))
         :stop  (fn [] (.stop server))}))))