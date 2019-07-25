package io.zhudy.duic.service

import io.mockk.*
import io.zhudy.duic.ApplicationUnusableEvent
import io.zhudy.duic.ApplicationUsableEvent
import io.zhudy.duic.repository.ServerRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.SpringApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.ApplicationEventMulticaster
import org.springframework.context.support.GenericXmlApplicationContext
import reactor.core.publisher.Mono
import java.util.concurrent.CountDownLatch

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
internal class ServerCheckerTests {

    @Test
    fun usable() {
        val serverRepository = mockk<ServerRepository>()
        val multicaster = mockk<ApplicationEventMulticaster>()
        every { serverRepository.findDbVersion() } returns Mono.just("test")

        //
        val event = slot<ApplicationUsableEvent>()
        every { multicaster.multicastEvent(capture(event)) } returns Unit

        val checker = ServerChecker(serverRepository, multicaster)
        val spykChecker = spyk(checker)

        val downLatch = mockk<CountDownLatch>()
        every { spykChecker["downLatch"]() } returns downLatch
        every { downLatch.countDown() } returns Unit
        every { downLatch.await(any(), any()) } returns true

        spykChecker.springReadyEvent(ApplicationReadyEvent(SpringApplication(), null, GenericXmlApplicationContext()))

        assertThat(event.captured).isNotNull

        verify {
            downLatch.countDown()
            downLatch.await(any(), any())
        }
    }

    @Test
    fun unusable() {
        val serverRepository = mockk<ServerRepository>()
        val multicaster = mockk<ApplicationEventMulticaster>()
        every { serverRepository.findDbVersion() } returns Mono.just("test")

        //
        val event = slot<ApplicationUnusableEvent>()
        every { multicaster.multicastEvent(capture(event)) } returns Unit

        val checker = ServerChecker(serverRepository, multicaster)
        val spykChecker = spyk(checker)

        val downLatch = mockk<CountDownLatch>()
        every { spykChecker["downLatch"]() } returns downLatch
        every { downLatch.countDown() } returns Unit
        every { downLatch.await(any(), any()) } returns false

        spykChecker.springReadyEvent(ApplicationReadyEvent(SpringApplication(), null, GenericXmlApplicationContext()))

        assertThat(event.captured).isNotNull

        verify {
            downLatch.countDown()
            downLatch.await(any(), any())
        }
    }

    @Test
    fun `unusable InterruptedException`() {
        val serverRepository = mockk<ServerRepository>()
        val multicaster = mockk<ApplicationEventMulticaster>()
        every { serverRepository.findDbVersion() } returns Mono.just("test")

        //
        val event = slot<ApplicationUnusableEvent>()
        every { multicaster.multicastEvent(capture(event)) } returns Unit

        val checker = ServerChecker(serverRepository, multicaster)
        val spykChecker = spyk(checker)

        val downLatch = mockk<CountDownLatch>()
        every { spykChecker["downLatch"]() } returns downLatch
        every { downLatch.countDown() } returns Unit
        every { downLatch.await(any(), any()) } throws InterruptedException()

        spykChecker.springReadyEvent(ApplicationReadyEvent(SpringApplication(), null, GenericXmlApplicationContext()))

        assertThat(event.captured).isNotNull

        verify {
            downLatch.countDown()
            downLatch.await(any(), any())
        }
    }

    @Test
    fun downLatch() {
        val serverRepository = mockk<ServerRepository>()
        val multicaster = mockk<ApplicationEventMulticaster>()
        val checker = ServerChecker(serverRepository, multicaster)
        val cdl = checker.downLatch()
        assertThat(cdl.count).isEqualTo(1)
    }
}