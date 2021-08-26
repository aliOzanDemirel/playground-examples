package app

import ratpack.groovy.Groovy
import ratpack.server.RatpackServer

class Main {
    static void main(String... args) throws Exception {
        RatpackServer.start(Groovy.Script.appWithArgs(args))
    }
}
