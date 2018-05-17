package io.zhudy.duic.domain

import org.joda.time.DateTime

/**
 * 服务主机信息。
 *
 * @property host 主机名
 * @property port 主机端口
 * @property initAt 初始化时间
 * @property activeAt 最后活跃时间如果于当前时间相关超过1分钟则认为该服务不处于活跃中
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
class Server(
        val host: String = "",
        val port: Int = 0,
        val initAt: DateTime,
        val activeAt: DateTime
)