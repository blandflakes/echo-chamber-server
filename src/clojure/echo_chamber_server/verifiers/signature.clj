(ns echo-chamber-server.verifiers.signature
  (:import (io.undertow.server HttpServerExchange)
           (echochamber.server.verifiers SignatureVerifier)))

(defn #^:private get-header [^HttpServerExchange exchange ^String header-name]
  (.getFirst (.getRequestHeaders exchange) header-name))

(defn verifier []
  (let [sig-verifier (SignatureVerifier.)]
    (fn [^HttpServerExchange exchange _request-envelope request-bytes]
      (let [signature (get-header exchange "signature")
            signing-certificate-url (get-header exchange "SignatureCertChainUrl")]
        (.verify sig-verifier signature signing-certificate-url request-bytes)))))