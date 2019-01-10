package io.zhudy.duic

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationEvent

/**
 * 应用未启动成功无法正常使用的事件类型。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
class ApplicationUnusableEvent(val applicationContext: ApplicationContext) : ApplicationEvent(applicationContext)