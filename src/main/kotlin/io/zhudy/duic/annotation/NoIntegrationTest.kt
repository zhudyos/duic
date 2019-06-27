package io.zhudy.duic.annotation

import org.springframework.context.annotation.Profile
import java.lang.annotation.Documented

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention
@Documented
@Profile("!integration-test")
annotation class NoIntegrationTest