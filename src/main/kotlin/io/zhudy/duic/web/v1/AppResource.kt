package io.zhudy.duic.web.v1

import com.memeyule.cryolite.core.BizCode
import com.memeyule.cryolite.core.BizCodeException
import io.javalin.Context
import io.zhudy.duic.service.AppService
import io.zhudy.duic.web.pathString
import org.springframework.stereotype.Controller

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Controller
class AppResource(val appService: AppService) {

    /**
     *
     */
    fun getConfigState(ctx: Context) {
        val name = ctx.pathString("name")
        val profiles = getProfilesParam(ctx)
        ctx.json(mapOf("state" to appService.getConfigState(name, profiles)))
    }

    /**
     *
     */
    fun loadSpringCloudConfig(ctx: Context) {
        val name = ctx.pathString("name")
        val profiles = getProfilesParam(ctx)
        ctx.json(appService.loadSpringCloudConfig(name, profiles))
    }

    /**
     *
     */
    fun loadConfigByNp(ctx: Context) {
        val name = ctx.pathString("name")
        val profiles = getProfilesParam(ctx)
        ctx.json(appService.loadConfigByNp(name, profiles))
    }

    /**
     *
     */
    fun loadConfigByNpAndKey(ctx: Context) {
        val name = ctx.pathString("name")
        val profiles = getProfilesParam(ctx)
        val key = ctx.param("key")?.trim() ?: throw BizCodeException(BizCode.Classic.C_999, "缺少 key 参数")
        ctx.json(appService.loadConfigByNpAndKey(name, profiles, key) ?: emptyMap<Any, Any>())
    }

    private fun getProfilesParam(ctx: Context): List<String> {
        val profiles = ctx.pathString("profiles")?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
        if (profiles == null || profiles.isEmpty()) {
            throw BizCodeException(BizCode.Classic.C_999, "缺少 profiles 参数")
        }
        return profiles
    }
}