package io.zhudy.duic

import com.memeyule.cryolite.core.BizCode

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
enum class BizCodes(override val code: Int, override val msg: String) : BizCode {

    /**
     *
     */
    C_1000(1000, "未找到应用"),
    C_1001(1001, "未找到应用 profile"),
    C_1002(1001, "未找到配置项"),
    C_1003(1003, "修改 App Content 失败")

    //
    ;
}