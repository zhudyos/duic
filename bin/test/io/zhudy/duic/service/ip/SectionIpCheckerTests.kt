package io.zhudy.duic.service.ip

import io.zhudy.duic.utils.IpUtils
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
internal class SectionIpCheckerTests {

    @Test
    fun check() {
        val checker = SectionIpChecker(from = 3232235777, to = 3232236031) // 192.168.1.1 - 192.168.1.255
        assertTrue(checker.check(IpUtils.ipv4ToLong("192.168.1.1")))
        assertTrue(checker.check(IpUtils.ipv4ToLong("192.168.1.2")))
        assertTrue(checker.check(IpUtils.ipv4ToLong("192.168.1.255")))
    }

    @Test
    fun `check false`() {
        val checker = SectionIpChecker(from = 3232235777, to = 3232236031) // 192.168.1.1 - 192.168.1.255
        assertFalse(checker.check(IpUtils.ipv4ToLong("192.168.2.1")))
        assertFalse(checker.check(IpUtils.ipv4ToLong("192.168.2.2")))
        assertFalse(checker.check(IpUtils.ipv4ToLong("192.168.2.255")))
    }
}