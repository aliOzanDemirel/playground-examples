package app

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object JsonMapper {

    val mapper: ObjectMapper = jacksonObjectMapper()

    init {
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true)
    }
}
