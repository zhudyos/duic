package io.zhudy.duic.service.ip

import io.zhudy.duic.service.IpChecker

/**
 * 一段 IP 校验器(192.168.0.1-192.168.0.255)。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
class SectionIpChecker(val from: Long, val to: Long) : IpChecker {

    /**
     * 包涵该 IP 则返回 `true` 反之返回 `false`。
     */
    override fun check(ip: Long) = ip in from..to
}