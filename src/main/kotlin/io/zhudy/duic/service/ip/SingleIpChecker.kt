package io.zhudy.duic.service.ip

import io.zhudy.duic.service.IpChecker

/**
 * 单个 IP 校验器（127.0.0.1）。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
class SingleIpChecker(val ip: Long) : IpChecker {

    /**
     *
     */
    override fun check(ip: Long) = ip == this.ip
}