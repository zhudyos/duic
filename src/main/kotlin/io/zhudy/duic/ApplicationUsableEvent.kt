package io.zhudy.duic

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationEvent

/**
 * 应用启动成功并且为可用状态时发送该事件。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Suppress("MemberVisibilityCanBePrivate")
class ApplicationUsableEvent(val applicationContext: ApplicationContext) : ApplicationEvent(applicationContext)