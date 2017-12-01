package io.zhudy.duic.web.admin

import io.javalin.Context
import io.zhudy.duic.domain.App
import io.zhudy.duic.service.AppService
import io.zhudy.duic.utils.WebUtils
import org.springframework.stereotype.Controller

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Controller
class AdminResource(
        val appService: AppService
) {

    data class UpdateAppVo(val content: String)

    /**
     * 登录.
     */
    fun login(ctx: Context) {

    }

    /**
     * 保存应用.
     */
    fun saveApp(ctx: Context) {
        val app = ctx.bodyAsClass(App::class.java)
        appService.save(app)
        ctx.status(201)
    }

    /**
     *
     */
    fun updateApp(ctx: Context) {
        val app = ctx.bodyAsClass(App::class.java)
        appService.updateContent(app)
        ctx.status(204)
    }

    fun deleteApp(ctx: Context) {
    }

    /**
     * 查询单个应用配置.
     */
    fun findOne(ctx: Context) {
        val name = ctx.param("name") ?: throw IllegalArgumentException("缺少 name 参数")
        val profile = ctx.param("profile") ?: throw IllegalArgumentException("缺少 profile 参数")
        ctx.json(appService.findOne(name, profile))
    }

    /**
     *
     */
    fun findAll(ctx: Context) {

        ctx.json(appService.findPage(WebUtils.getPage(ctx)))
    }
}