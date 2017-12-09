package io.zhudy.duic.web.security

import io.javalin.Context
import io.javalin.security.Role

/**
 * 角色接口定义.
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
interface RoleInterceptor : Role {

    /**
     * 确认请求是否有操作权限.
     */
    @Throws(Exception::class)
    fun handle(ctx: Context)

}