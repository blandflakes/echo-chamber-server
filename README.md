# echo-chamber-server

An undertow server for hosting multitenant skills.

The general idea here is to get out of Ring-land into
a more feature-ful server which has fewer dependency
alignments involved in an upgrade. An additional
boon is the porting of the SignatureVerifier from
the [Alexa Skills Kit for Java](https://github.com/alexa/alexa-skills-kit-sdk-for-java)
so that we don't have to rely on the dependency closure
of that project for a single feature.

## Usage

`server.clj` contains the significant method - you
can pass any number of skills with routes to the `app-server`
function and they'll be hosted. The function returns a map containing
`:start` and `:stop` keys whose values are functions for
those behaviors.

So, to host some skills:

    (let [backend (app-server ip port {:skill-fn app/skill :verifiers [(signature/verifier) (timestamp/verifier)]})]
          (.addShutdownHook (Runtime/getRuntime) (Thread. ((:stop backend))))
          ((:start backend)))))

Multiple maps can be passed after the `port` argument, but
they must include a `:route` key in their map or the handler
will attempt to host them at "/".

## TODO

* This README could use some work
* And this project could use some tests
* Support for request logging
* Expose SSL config for those without reverse proxy

## License

Copyright © 2018 blandflakes

Apache Licensed
