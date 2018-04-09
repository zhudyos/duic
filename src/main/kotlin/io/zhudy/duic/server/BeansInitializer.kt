package io.zhudy.duic.server

import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class BeansInitializer : ApplicationContextInitializer<GenericApplicationContext> {

    override fun initialize(applicationContext: GenericApplicationContext) {
        beans().initialize(applicationContext)
    }
}