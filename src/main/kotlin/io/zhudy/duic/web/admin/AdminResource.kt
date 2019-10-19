/**
 * Copyright 2017-2019 the original author or authors
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
import io.zhudy.duic.domain.AppContentHistory
import io.zhudy.duic.domain.AppPair
import io.zhudy.duic.domain.User
import io.zhudy.duic.dto.ResetPasswordDto
import io.zhudy.duic.service.AppService
import io.zhudy.duic.service.ServerService
import io.zhudy.duic.service.UserService
import io.zhudy.duic.utils.WebUtils
import io.zhudy.duic.vo.AppVo
import io.zhudy.duic.web.pathString
import io.zhudy.duic.web.security.userContext
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.function.BodyExtractors
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import org.valiktor.functions.hasSize
import org.valiktor.functions.matches
import org.valiktor.validate
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.Instant
import java.util.*

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Controller
class AdminResource(
        val userService: UserService,
        val appService: AppService,
        val jwtAlgorithm: Algorithm,
        val serverService: ServerService
) {

    data class LoginUser(val email: String, val password: String)
    data class UpdatePassword(val oldPassword: String, val newPassword: String)

    /**
     * 登录.
     */
    fun login(request: ServerRequest): Mono<ServerResponse> = request.body(BodyExtractors.toMono(LoginUser::class.java)).flatMap {
        userService.login(it.email, it.password).flatMap { user ->
            val expiresAt = Instant.now().plus(Duration.ofSeconds(Config.jwt.expiresIn))
            val token = JWT.create().withJWTId(user.email)
                    .withExpiresAt(Date.from(expiresAt))
                    .sign(jwtAlgorithm)

            ok().cookie(
                    ResponseCookie.from("token", token)
                            .maxAge(Config.jwt.expiresIn)
                            .httpOnly(true)
                            .path("/")
                            .build()
            ).cookie(
                    ResponseCookie.from("email", user.email)
                            .maxAge(Config.jwt.expiresIn)
                            .path("/")
                            .build()
            ).build()
        }
    }

    // ======================================= USER ====================================================== \\

    /**
     * 返回 `root` 用户登录名.
     */
    fun rootUser(request: ServerRequest): Mono<ServerResponse> = ok().syncBody(mapOf("root" to Config.rootEmail))

    /**
     * 保存用户.
     */
    fun insertUser(request: ServerRequest): Mono<ServerResponse> = request.bodyToMono(User::class.java).flatMap {
        if (it.email.isEmpty()) {
            throw BizCodeException(BizCode.Classic.C_999, "email 不能为空")
        }
        if (it.password.isEmpty()) {
            throw BizCodeException(BizCode.Classic.C_999, "密码不能为空")
        }

        userService.insert(it).then(ok().build())
    }

    /**
     * 删除用户.
     */
    fun deleteUser(request: ServerRequest): Mono<ServerResponse> = userService.delete(request.pathString("email"))
            .then(noContent().build())

    /**
     *
     */
    fun updateUserPassword(request: ServerRequest): Mono<ServerResponse> = request.bodyToMono(UpdatePassword::class.java)
            .flatMap {
                val userContext = request.userContext()
                userService.updatePassword(userContext.email, it.oldPassword, it.newPassword)
                        .then(noContent().build())
            }

    /**
     * 重置用户密码.
     */
    fun resetUserPassword(request: ServerRequest): Mono<ServerResponse> = request.bodyToMono(ResetPasswordDto::class.java)
            .flatMap(userService::resetPassword)
            .then(noContent().build())

    /**
     */
    fun findPageUser(request: ServerRequest): Mono<ServerResponse> = userService.findPage(WebUtils.getPage(request))
            .flatMap(ok()::syncBody)

    /**
     *
     */
    fun findAllEmail(request: ServerRequest): Mono<ServerResponse> = userService.findAllEmail().collectList()
            .flatMap(ok()::syncBody)
    // ======================================= USER ====================================================== //


    // ======================================= APP ======================================================= \\

    /**
     * 保存应用.
     */
    fun insertApp(request: ServerRequest): Mono<ServerResponse> = request.bodyToMono(AppVo.NewApp::class.java)
            .doOnNext {
                validate(it) {
                    val standardChar = "/^[A-Za-z0-9\\.\\-]+\$/".toRegex()
                    validate(AppVo.NewApp::name).hasSize(min = 1, max = 64).matches(standardChar)
                    validate(AppVo.NewApp::profile).hasSize(min = 1, max = 64).matches(standardChar)
                    validate(AppVo.NewApp::description).hasSize(max = 1024)
                    validate(AppVo.NewApp::token).hasSize(max = 64)
                    validate(AppVo.NewApp::ipLimit).hasSize(max = 1024)
                    validate(AppVo.NewApp::users).hasSize(max = 50)
                }
            }
            .flatMap(appService::insert)
            .then(ok().build())

    /**
     *
     */
    fun updateApp(request: ServerRequest): Mono<ServerResponse> = request.bodyToMono(AppVo.UpdateBasicInfo::class.java)
            .flatMap { vo ->
                val ap = AppPair(request.pathVariable("name"), request.pathVariable("profile"))
                appService.update(ap, vo, request.userContext())
                        .map { v -> mapOf("v" to v) }
                        .flatMap(ok()::bodyValue)
            }

    /**
     *
     */
    fun updateAppContent(request: ServerRequest): Mono<ServerResponse> = request.bodyToMono(AppVo.UpdateContent::class.java)
            .flatMap { vo ->
                val ap = AppPair(request.pathVariable("name"), request.pathVariable("profile"))
                appService.updateContent(ap, vo, request.userContext())
                        .map { v -> mapOf("v" to v) }
                        .flatMap(ok()::bodyValue)
            }

    /**
     *
     */
    fun deleteApp(request: ServerRequest): Mono<ServerResponse> {
        val name = request.pathString("name")
        val profile = request.pathString("profile")
        return appService.delete(AppPair(name = name, profile = profile), request.userContext())
                .then(noContent().build())
    }

    /**
     * 查询单个应用配置.
     */
    fun findOneApp(request: ServerRequest): Mono<ServerResponse> = appService.findOne(getAppPair(request))
            .flatMap(ok()::bodyValue)

    /**
     * 查询用户的 apps.
     */
    fun findAppByUser(request: ServerRequest): Mono<ServerResponse> = appService.search(
            request.queryParam("q").orElse(null),
            WebUtils.getPage(request),
            request.userContext()
    ).flatMap(ok()::bodyValue)

    /**
     * 搜索配置。
     */
    fun searchAppByUser(request: ServerRequest): Mono<ServerResponse> {
        val page = WebUtils.getPage(request)
        val q = request.queryParam("q").orElse("").trim()
        return appService.search(q, page, request.userContext()).flatMap(ok()::syncBody)
    }

    /**
     * 查询 App 内容历史记录.
     */
    fun findAppContentHistory(request: ServerRequest): Mono<ServerResponse> {
        return ok().body(appService.findLast50History(getAppPair(request), request.userContext()),
                AppContentHistory::class.java)
    }

    fun findAllNames(request: ServerRequest): Mono<ServerResponse> = appService.findAllNames().collectList()
            .flatMap(ok()::bodyValue)

    fun findProfilesByName(request: ServerRequest): Mono<ServerResponse> = appService.findProfilesByName(request.pathString("name"))
            .collectList()
            .flatMap(ok()::bodyValue)

    /**
     *
     */
    fun findLastDataTime(request: ServerRequest): Mono<ServerResponse> = appService.findLastDataTime()
            .map { mapOf("last_data_time" to it) }
            .flatMap(ok()::bodyValue)

    // ======================================= APP ======================================================= //

    // ======================================= SERVER ==================================================== //

    fun loadServerStates(request: ServerRequest): Mono<ServerResponse> = ok().body(serverService.loadServerStates())

    // ======================================= SERVER ==================================================== //

    private fun getAppPair(request: ServerRequest) = AppPair(
            name = request.pathVariable("name"),
            profile = request.pathVariable("profile")
    )
}