/**
 * Copyright 2017-2018 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zhudy.duic.web.admin

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.zhudy.duic.BizCode
import io.zhudy.duic.BizCodeException
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
import io.zhudy.duic.web.security.userContext
import org.joda.time.DateTime
import org.springframework.http.ResponseCookie
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
    fun login(request: ServerRequest) = request.body(BodyExtractors.toMono(LoginUser::class.java)).flatMap {
        userService.login(it.email, it.password).flatMap {
            val token = JWT.create().withJWTId(it.email)
                    .withExpiresAt(DateTime.now().plusMinutes(Config.Jwt.expiresIn).toDate())
                    .sign(jwtAlgorithm)

            ok().cookie(
                    ResponseCookie.from("token", token)
                            .maxAge(Config.Jwt.expiresIn.toLong())
                            .httpOnly(true)
                            .path("/")
                            .build()
            ).cookie(
                    ResponseCookie.from("email", it.email)
                            .maxAge(Config.Jwt.expiresIn.toLong())
                            .path("/")
                            .build()
            ).build()
        }
    }

    // ======================================= USER ====================================================== \\

    /**
     * 返回 `root` 用户登录名.
     */
    fun rootUser(request: ServerRequest) = ok().body(mapOf("root" to Config.rootEmail))

    /**
     * 保存用户.
     */
    fun insertUser(request: ServerRequest) = request.body(BodyExtractors.toMono(User::class.java)).flatMap {
        if (it.email.isEmpty()) {
            throw BizCodeException(BizCode.Classic.C_999, "email 不能为空")
        }
        if (it.password.isEmpty()) {
            throw BizCodeException(BizCode.Classic.C_999, "密码不能为空")
        }

        userService.insert(it).flatMap {
            ok().build()
        }
    }

    /**
     * 删除用户.
     */
    fun deleteUser(request: ServerRequest) = userService.delete(request.pathString("email")).flatMap {
        noContent().build()
    }

    /**
     *
     */
    fun updateUserPassword(request: ServerRequest) = request.bodyToMono(UpdatePassword::class.java).flatMap {
        val userContext = request.userContext()
        userService.updatePassword(userContext.email, it.oldPassword, it.newPassword).flatMap {
            noContent().build()
        }
    }

    /**
     * 重置用户密码.
     */
    fun resetUserPassword(request: ServerRequest) = request.bodyToMono(ResetPasswordDto::class.java).flatMap {
        userService.resetPassword(it).flatMap {
            noContent().build()
        }
    }

    /**
     */
    fun findPageUser(request: ServerRequest) = userService.findPage(WebUtils.getPage(request)).flatMap {
        ok().body(it)
    }


    /**
     *
     */
    fun findAllEmail(request: ServerRequest) = userService.findAllEmail().collectList().flatMap {
        ok().body(it)
    }
    // ======================================= USER ====================================================== //


    // ======================================= APP ======================================================= \\

    /**
     * 保存应用.
     */
    fun insertApp(request: ServerRequest) = request.bodyToMono(App::class.java).flatMap {
        if (it.name.isEmpty()) {
            throw BizCodeException(BizCode.Classic.C_999, "应用名称不能为空")
        }
        if (it.profile.isEmpty()) {
            throw BizCodeException(BizCode.Classic.C_999, "应用环境不能为空")
        }

        appService.insert(it).flatMap {
            ok().build()
        }
    }

    /**
     * 从已经存在的应用信息中插入一个应用。
     */
    fun insertAppForApp(request: ServerRequest) = request.bodyToMono(App::class.java).flatMap { newApp ->
        appService.findOne(request.pathString("name"), request.pathString("profile")).flatMap {
            if (newApp.name.isEmpty()) {
                throw BizCodeException(BizCode.Classic.C_999, "应用名称不能为空")
            }
            if (newApp.profile.isEmpty()) {
                throw BizCodeException(BizCode.Classic.C_999, "应用环境不能为空")
            }
            newApp.content = it.content

            appService.insert(newApp).flatMap {
                ok().build()
            }
        }
    }

    /**
     *
     */
    fun updateApp(request: ServerRequest) = request.bodyToMono(App::class.java).flatMap {
        if (it.name.isEmpty()) {
            throw BizCodeException(BizCode.Classic.C_999, "应用名称不能为空")
        }
        if (it.profile.isEmpty()) {
            throw BizCodeException(BizCode.Classic.C_999, "应用环境不能为空")
        }

        appService.update(it, request.userContext()).flatMap {
            ok().body(mapOf("v" to it))
        }
    }

    /**
     *
     */
    fun updateAppContent(request: ServerRequest) = request.bodyToMono(App::class.java).flatMap {
        appService.updateContent(it, request.userContext()).flatMap {
            ok().body(mapOf("v" to it))
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