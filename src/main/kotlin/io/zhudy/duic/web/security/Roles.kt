package io.zhudy.duic.web.security

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.memeyule.cryolite.core.BizCode
import com.memeyule.cryolite.core.BizCodeException
import io.javalin.Context
import io.zhudy.duic.Config
import io.zhudy.duic.UserContext

/**
 * 用户认证.
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
object AuthUserInterceptor : RoleInterceptor {

    override fun handle(ctx: Context) {
        val token = ctx.cookie("token")
        if (token.isNullOrEmpty()) {
            throw BizCodeException(BizCode.Classic.C_401, "缺少 token")
        }

        val jwt = try {
            JWT.decode(token)
        } catch (e: JWTDecodeException) {
            throw BizCodeException(BizCode.Classic.C_401, "解析 token 失败 ${e.message}")
        }

        if ((jwt.expiresAt?.time ?: 0) < System.currentTimeMillis()) {
            throw BizCodeException(BizCode.Classic.C_401, "token 已经过期请重新登录")
        }

        if (jwt.id.isNullOrEmpty()) {
            throw BizCodeException(BizCode.Classic.C_401, "错误的 token")
        }

        ctx.attribute(UserContext.CONTEXT_KEY, object : UserContext {
            override val email: String
                get() = jwt.id
            override val isRoot: Boolean
                get() = Config.rootEmail == email
        })
    }
}

/**
 * 超过用户校验.
 */
object RootUserInterceptor : RoleInterceptor {

    override fun handle(ctx: Context) {
        val userContext = ctx.userContext()
        if (userContext.email != Config.rootEmail) {
            throw BizCodeException(BizCode.Classic.C_403)
        }
    }
}

