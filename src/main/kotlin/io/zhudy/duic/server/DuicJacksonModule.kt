package io.zhudy.duic.server

import com.fasterxml.jackson.databind.module.SimpleModule
import io.zhudy.duic.utils.PageSerializer
import org.springframework.data.domain.Page
import org.springframework.stereotype.Component

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Component
class DuicJacksonModule : SimpleModule("duic") {

    init {
        addSerializer(Page::class.java, PageSerializer)
    }

}