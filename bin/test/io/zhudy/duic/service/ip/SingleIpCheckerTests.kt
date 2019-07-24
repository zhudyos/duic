package io.zhudy.duic.service.ip

import io.zhudy.duic.utils.IpUtils
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
internal class SingleIpCheckerTests {

    @Test
    fun check() {
        val checker = SingleIpChecker(ip = 3232235899) // 192.168.1.123
        val rs = checker.check(IpUtils.ipv4ToLong("192.168.1.123"))
        assertTrue(rs)
    }

    @Test
    fun `check false`() {
        val checker = SingleIpChecker(ip = 3232235900) // 192.168.1.124
        val rs = checker.check(IpUtils.ipv4ToLong("192.168.1.123"))
        assertFalse(rs)
    }

}