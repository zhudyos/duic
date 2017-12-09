package io.zhudy.duic.web.security

import io.javalin.Context
import io.javalin.Handler
import io.javalin.security.AccessManager
import io.javalin.security.Role

/**
 * 访问控制.
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
object AccessManagerImpl : AccessManager {

    override fun manage(handler: Handler, ctx: Context, permittedRoles: List<Role>) {
        permittedRoles.forEach {
            (it as RoleInterceptor).handle(ctx)
        }

        handler.handle(ctx)
    }
}