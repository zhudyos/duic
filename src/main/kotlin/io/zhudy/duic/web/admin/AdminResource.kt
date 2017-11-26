package io.zhudy.duic.web.admin

import io.javalin.Context
import io.zhudy.duic.domain.App
import io.zhudy.duic.service.AppService
import org.springframework.stereotype.Controller

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Controller
class AdminResource(
        val appService: AppService
) {

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
    }

    fun deleteApp(ctx: Context) {

    }

}