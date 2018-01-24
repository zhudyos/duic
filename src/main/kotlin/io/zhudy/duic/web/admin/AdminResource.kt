package io.zhudy.duic.web.admin

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.zhudy.duic.Config
import io.zhudy.duic.domain.App
import io.zhudy.duic.domain.AppContentHistory
import io.zhudy.duic.domain.User
import io.zhudy.duic.dto.ResetPasswordDto
import io.zhudy.duic.service.AppService
import io.zhudy.duic.service.UserService
import io.zhudy.duic.utils.WebUtils
import io.zhudy.duic.web.body
import io.zhudy.duic.web.pathString
import io.zhudy.duic.web.queryTrimString
import io.zhudy.duic.web.security.userContext
import org.joda.time.DateTime
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.function.BodyExtractors
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Controller
class AdminResource(
        val userService: UserService,
        val appService: AppService,
        val jwtAlgorithm: Algorithm
) {

    data class LoginUser(val email: String, val password: String)
    data class UpdatePassword(val oldPassword: String, val newPassword: String)

    /**
     * 登录.
     */
    fun login(request: ServerRequest): Mono<ServerResponse> = request.body(BodyExtractors.toMono(LoginUser::class.java)).flatMap {
        userService.login(it.email, it.password).flatMap {
            val token = JWT.create().withJWTId(it.email)
                    .withExpiresAt(DateTime.now().plusMinutes(Config.Jwt.expiresIn).toDate())
                    .sign(jwtAlgorithm)
            ok().body(mapOf("token" to token))
        }
    }

    // ======================================= USER ====================================================== \\

    /**
     * 返回 `root` 用户登录名.
     */
    fun rootUser(request: ServerRequest): Mono<ServerResponse> = ok().body(mapOf("root" to Config.rootEmail))

    /**
     * 保存用户.
     */
    fun insertUser(request: ServerRequest): Mono<ServerResponse> = request.body(BodyExtractors.toMono(User::class.java)).flatMap {
        userService.insert(it).flatMap {
            ok().build()
        }
    }

    /**
     * 删除用户.
     */
    fun deleteUser(request: ServerRequest): Mono<ServerResponse> {
        val email = request.pathString("email")
        return userService.delete(email).flatMap {
            noContent().build()
        }
    }

    /**
     *
     */
    fun updateUserPassword(request: ServerRequest): Mono<ServerResponse> {
        return request.bodyToMono(UpdatePassword::class.java).flatMap {
            val userContext = request.userContext()
            userService.updatePassword(userContext.email, it.oldPassword, it.newPassword).flatMap {
                noContent().build()
            }
        }
    }

    /**
     * 重置用户密码.
     */
    fun resetUserPassword(request: ServerRequest): Mono<ServerResponse> = request.bodyToMono(ResetPasswordDto::class.java).flatMap {
        userService.resetPassword(it).flatMap {
            noContent().build()
        }
    }

    /**
     *
     */
    fun findPageUser(request: ServerRequest): Mono<ServerResponse> = userService.findPage(WebUtils.getPage(request)).flatMap {
        ok().body(it)
    }


    /**
     *
     */
    fun findAllEmail(request: ServerRequest): Mono<ServerResponse> = userService.findAllEmail().collectList().flatMap {
        ok().body(it)
    }
    // ======================================= USER ====================================================== //


    // ======================================= APP ======================================================= \\

    /**
     * 保存应用.
     */
    fun insertApp(request: ServerRequest): Mono<ServerResponse> {
        return request.bodyToMono(App::class.java).flatMap {
            appService.insert(it).flatMap {
                ok().build()
            }
        }
    }

    /**
     *
     */
    fun updateApp(request: ServerRequest): Mono<ServerResponse> {
        return request.bodyToMono(App::class.java).flatMap {
            appService.update(it, request.userContext()).flatMap {
                ok().body(mapOf("v" to it))
            }
        }
    }

    /**
     *
     */
    fun updateAppContent(request: ServerRequest): Mono<ServerResponse> {
        return request.bodyToMono(App::class.java).flatMap {
            appService.updateContent(it, request.userContext()).flatMap {
                ok().body(mapOf("v" to it))
            }
        }
    }

    /**
     *
     */
    fun deleteApp(request: ServerRequest): Mono<ServerResponse> {
        val name = request.pathString("name")
        val profile = request.pathString("profile")
        return appService.delete(App(name = name, profile = profile), request.userContext()).flatMap {
            noContent().build()
        }
    }

    /**
     * 查询单个应用配置.
     */
    fun findOneApp(request: ServerRequest): Mono<ServerResponse> {
        val name = request.pathString("name")
        val profile = request.pathString("profile")

        return appService.findOne(name, profile).flatMap {
            ok().body(it)
        }
    }

    /**
     * 查询用户的 apps.
     */
    fun findAppByUser(request: ServerRequest): Mono<ServerResponse> {
        val page = WebUtils.getPage(request)
        return appService.findPageByUser(page, request.userContext()).flatMap {
            ok().body(it)
        }
    }

    /**
     * 搜索配置。
     */
    fun searchAppByUser(request: ServerRequest): Mono<ServerResponse> {
        val page = WebUtils.getPage(request)
        val q = request.queryParam("q").orElse("").trim()
        return appService.searchPageByUser(q, page, request.userContext()).flatMap {
            ok().body(it)
        }
    }

    /**
     * 查询 App 内容历史记录.
     */
    fun findAppContentHistory(request: ServerRequest): Mono<ServerResponse> {
        val name = request.pathString("name")
        val profile = request.pathString("profile")

        return ok().body(appService.findLast50History(name, profile, request.userContext()),
                AppContentHistory::class.java)
    }
    // ======================================= APP ======================================================= //
}