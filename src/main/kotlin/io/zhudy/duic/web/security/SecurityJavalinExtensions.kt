package io.zhudy.duic.web.security

import com.memeyule.cryolite.core.BizCode
import com.memeyule.cryolite.core.BizCodeException
import io.javalin.Context
import io.zhudy.duic.UserContext

/**
 * 认证用户上下文.
 *
 * @throws BizCodeException 如果未认证将返回 [BizCode.Classic.C_401] 错误
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
fun Context.userContext(): UserContext = attribute(UserContext.CONTEXT_KEY) ?: throw BizCodeException(BizCode.Classic.C_401)
