(defproject echo-chamber-server "0.1.0"
  :description "Server for hosting echo applications in a multi-tenant fashion"
  :license {:name "Apache License"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :source-paths ["src/clojure"]
  :java-source-paths ["src/java"]
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [clj-time "0.14.4"]
                 [cheshire "5.8.0"]
                 [commons-codec/commons-codec "1.11"]
                 [io.undertow/undertow-core "2.0.7.Final"]])
