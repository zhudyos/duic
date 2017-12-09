package io.zhudy.duic.web.admin

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.javalin.Context
import io.zhudy.duic.Config
import io.zhudy.duic.domain.App
import io.zhudy.duic.domain.User
import io.zhudy.duic.dto.UpdatePasswordDto
import io.zhudy.duic.service.AppService
import io.zhudy.duic.service.UserService
import io.zhudy.duic.utils.WebUtils
import io.zhudy.duic.web.formString
import io.zhudy.duic.web.pathString
import org.joda.time.DateTime
import org.springframework.stereotype.Controller

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Controller
class AdminResource(
        val userService: UserService,
        val appService: AppService,
        val jwtAlgorithm: Algorithm
) {

    /**
     * 登录.
     */
    fun login(ctx: Context) {
        val email = ctx.formString("email")
        val password = ctx.formString("password")
        userService.login(email, password)

        val token = JWT.create().withJWTId(email)
                .withExpiresAt(DateTime.now().plusMinutes(Config.Jwt.expiresIn).toDate())
                .sign(jwtAlgorithm)
        ctx.json(mapOf("token" to token))
    }

    // ======================================= USER ====================================================== \\

    /**
     * 返回 `root` 用户登录名.
     */
    fun rootUser(ctx: Context) {
        ctx.json(mapOf("root" to Config.rootEmail))
    }

    /**
     * 保存用户.
     */
    fun saveUser(ctx: Context) {
        val user = ctx.bodyAsClass(User::class.java)
        userService.save(user)
        ctx.status(201)
    }

    /**
     * 删除用户.
     */
    fun deleteUser(ctx: Context) {
        val email = ctx.pathString("email")
        userService.delete(email)
        ctx.status(204)
    }

    /**
     * 重置用户密码.
     */
    fun resetUserPassword(ctx: Context) {
        val dto = ctx.bodyAsClass(UpdatePasswordDto::class.java)
        userService.resetPassword(dto)
        ctx.status(204)
    }

    /**
     *
     */
    fun findPageUser(ctx: Context) {
        ctx.json(userService.findPage(WebUtils.getPage(ctx)))
    }

    // ======================================= USER ====================================================== //


    // ======================================= APP ======================================================= \\

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
    fun findOneApp(ctx: Context) {
        val name = ctx.pathString("name")
        val profile = ctx.pathString("profile")
        ctx.json(appService.findOne(name, profile))
    }

    /**
     *
     */
    fun findAllApp(ctx: Context) {
        ctx.json(appService.findPage(WebUtils.getPage(ctx)))
    }

    // ======================================= APP ======================================================= //
}