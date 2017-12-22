package io.zhudy.duic.server

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@ConditionalOnProperty(name = ["scheduling.enabled"], matchIfMissing = true)
@Configuration
@EnableScheduling
class SchedulingConfiguration