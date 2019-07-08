package io.zhudy.duic.service

import io.zhudy.duic.ApplicationUnusableEvent
import io.zhudy.duic.ApplicationUsableEvent
import io.zhudy.duic.annotation.NoIntegrationTest
import io.zhudy.duic.repository.ServerRepository
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.ApplicationEventMulticaster
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@NoIntegrationTest
@Service
class ServerChecker(
        private val serverRepository: ServerRepository,
        private val multicaster: ApplicationEventMulticaster
) {

    /**
     * `spring` 启动成功后立即校验数据库信息，如果数据库 3 秒未响应则返回错误。
     *
     * @see ApplicationReadyEvent
     */
    @EventListener(ApplicationReadyEvent::class)
    fun springReadyEvent(event: ApplicationReadyEvent) {
        val cdl = CountDownLatch(1)
        serverRepository.findDbVersion().subscribe { cdl.countDown() }

        val s = try {
            cdl.await(3, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            false
        }

        val e = if (s) ApplicationUsableEvent(event.applicationContext) else ApplicationUnusableEvent(event.applicationContext)
        multicaster.multicastEvent(e)
    }
}