(ns echo-chamber-server.verifiers.signature
  (:import (io.undertow.server HttpServerExchange)
           (echo_chamber_server.verifiers SignatureVerifier)))

(defn #^:private get-header [^HttpServerExchange exchange ^String header-name]
  (.get (.getRequestHeaders exchange) header-name))

(defn verifier []
  (let [sig-verifier (SignatureVerifier.)]
    (fn [^HttpServerExchange exchange _request-envelope request-bytes]
      (let [signature (get-header http-exchange "signature")
            signing-certificate-url (get-header http-exchange "signaturecerturl")]
        (.verify sig-verifier signature signing-certificate-url request-bytes)))))