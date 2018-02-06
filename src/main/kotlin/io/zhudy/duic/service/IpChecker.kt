package io.zhudy.duic.service

/**
 * IP 校验器。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
interface IpChecker {

    /**
     * 符合校验规则返回 `true` 反之返回 `false`。
     */
    fun check(ip: Long): Boolean

}