package io.zhudy.duic

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.test.context.ContextConfigurationAttributes
import org.springframework.test.context.ContextCustomizer
import org.springframework.test.context.ContextCustomizerFactory

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class SettingsContextCustomizerFactory : ContextCustomizerFactory {

    override fun createContextCustomizer(
        testClass: Class<*>,
        attributes: List<ContextConfigurationAttributes>
    ) = ContextCustomizer { context, _ ->

        // 集成测试环境禁用 spring-boot-auto-configuration
        TestPropertyValues.of(
            EnableAutoConfiguration.ENABLED_OVERRIDE_PROPERTY + "=false",
            "spring.test.database.replace=NONE"
        ).applyTo(context)
    }
}