package io.zhudy.duic.utils

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.springframework.data.domain.Page

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
object PageSerializer : StdSerializer<Page<*>>(Page::class.java, false) {

    override fun serialize(value: Page<*>, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()

        gen.writeFieldName("total_items")
        gen.writeRawValue(value.totalElements.toString())

        gen.writeFieldName("total_pages")
        gen.writeRawValue(value.totalPages.toString())

        gen.writeFieldName("items")
        gen.writeObject(value.content)

        gen.writeEndObject()
    }
}