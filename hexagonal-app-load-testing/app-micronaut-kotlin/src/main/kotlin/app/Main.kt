package app

import io.micronaut.runtime.Micronaut

class Main

fun main(args: Array<String>) {
    Micronaut.run(Main::class.java, *args)
}