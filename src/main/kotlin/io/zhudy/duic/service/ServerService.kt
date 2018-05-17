package io.zhudy.duic.service

import io.zhudy.duic.Config
import io.zhudy.duic.repository.ServerRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import reactor.core.scheduler.Schedulers

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Service
class ServerService(
        val serverRepository: ServerRepository
) {

    private val log = LoggerFactory.getLogger(ServerService::class.java)

    companion object {
        private const val PING_DELAY = 30 * 1000L
    }

    /**
     *
     */
    @EventListener(classes = [ApplicationReadyEvent::class])
    fun register() {
        serverRepository.register(Config.server.host, Config.server.port).doOnError {
            log.error("register 服务失败: ${Config.server.host}:${Config.server.port}", it)
        }.subscribe()
    }

    /**
     *
     */
    @EventListener(classes = [ContextClosedEvent::class])
    fun unregister() {
        serverRepository.register(Config.server.host, Config.server.port).doOnError {
            log.error("unregister 服务失败: ${Config.server.host}:${Config.server.port}", it)
        }.subscribe()
    }

    @Scheduled(initialDelay = PING_DELAY, fixedDelay = PING_DELAY)
    fun clockPing() {
        serverRepository.clean()
                .subscribeOn(Schedulers.parallel())
                .subscribe()

        serverRepository.ping(Config.server.host, Config.server.port)
                .subscribe()
    }

}