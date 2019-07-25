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
package io.zhudy.duic.service

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.difflib.text.DiffRowGenerator
import io.zhudy.duic.*
import io.zhudy.duic.domain.*
import io.zhudy.duic.dto.ServerRefreshDto
import io.zhudy.duic.dto.SpringCloudPropertySource
import io.zhudy.duic.dto.SpringCloudResponseDto
import io.zhudy.duic.repository.AppRepository
import io.zhudy.duic.repository.ServerRepository
import io.zhudy.duic.service.ip.SectionIpChecker
import io.zhudy.duic.service.ip.SingleIpChecker
import io.zhudy.duic.utils.IpUtils
import io.zhudy.duic.vo.RequestConfigVo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bson.types.ObjectId
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.mailer.MailerBuilder
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.DependsOn
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.yaml.snakeyaml.Yaml
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink
import reactor.core.scheduler.Schedulers
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * 应用配置逻辑处理实现。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Service
@DependsOn("io.zhudy.duic.Config")
class AppService(
        private val appRepository: AppRepository,
        private val serverRepository: ServerRepository,
        private val webClientBuilder: WebClient.Builder
) {

    companion object {

        private val reliableLog = LoggerFactory.getLogger("reliable")
        private val log = LoggerFactory.getLogger(AppService::class.java)
    }

    private val watchStateTimeout = 30 * 1000L
    private val watchRequestLimit = Config.concurrent.watchRequestLimit
    /**
     * 监控请求告警阀值。
     */
    private val watchReqWarnThreshold = (watchRequestLimit * Config.concurrent.warnRateThreshold).toInt()
    private val watchStateSinks = ConcurrentLinkedQueue<WatchStateSink>()
    private val updateAppQueue = LinkedBlockingQueue<String>()

    // 配置缓存
    private val appCaches = Caffeine.newBuilder().build<String, CachedApp>()

    @Volatile
    private var lastDataTime: Date? = null
    @Volatile
    private var lastAppHistoryCreatedAt = Date()
    private val yaml = Yaml()

    /**
     * 缓存的 APP 实例。
     */
    internal data class CachedApp(
            val name: String,
            val profile: String,
            val token: String,
            val ipLimit: List<IpChecker> = emptyList(),
            val v: Int,
            val properties: Map<Any, Any>
    )

    /**
     * 客户端监控状态的缓存对象。
     */
    private data class WatchStateSink(
            val sink: MonoSink<String>,
            val name: String,
            val profiles: List<String>,
            val state: String,
            val startTime: Long,

            /**
             * 如果 `done` 为 `true` 表示该操作已经成功完成，应该即时清理内存信息。
             */
            @Volatile
            var done: Boolean = false,
            var timeoutJob: Job? = null
    )

    @Scheduled(initialDelay = 1000, fixedDelayString = "%{duic.app.watch.updated.fixed-delay:60000}")
    fun watchApps() {
        refresh().subscribe()
    }

    @Scheduled(initialDelay = 1000, fixedDelayString = "%{duic.app.watch.deleted.fixed_delay::600000}")
    fun watchDeletedApps() {
        // 清理已经删除的 APP
        appRepository.findDeletedByCreatedAt(lastAppHistoryCreatedAt).doOnError {
            log.error("evict app config: ", it)
        }.doFinally {
            log.debug("lastAppHistoryCreatedAt={}", lastAppHistoryCreatedAt.time)
        }.subscribe {
            appCaches.invalidate(localKey(it.name, it.profile))
            lastAppHistoryCreatedAt = it.createdAt!!
        }
    }

    /**
     * 刷新内存的配置信息。
     *
     * @return 配置最后的更新时间戳（毫秒）
     */
    fun refresh(): Mono<Long> = Mono.create<Long> { sink ->
        // 更新 APP 配置信息
        if (lastDataTime == null) {
            findAll()
        } else {
            findByUpdatedAt(lastDataTime!!)
        }.doOnError {
            log.error("refresh app config: ", it)
        }.doFinally {
            log.debug("lastDataTime={}", lastDataTime?.time)
        }.doOnComplete {
            sink.success(lastDataTime?.time ?: 0L)
        }.subscribe {
            // 刷新缓存配置
            val k = localKey(it.name, it.profile)
            val app = mapToCachedApp(it)
            appCaches.put(k, app)
            lastDataTime = it.updatedAt
            updateAppQueue.offer(k)

            // 更新成功通知客户端最新的状态
            watchStateSinks.parallelStream()
                    .filter { e ->
                        e.name == app.name && e.profiles.indexOf(app.profile) >= 0
                    }
                    .forEach { wss ->
                        configState0(wss.name, wss.profiles)
                                .subscribeOn(Schedulers.parallel())
                                .subscribe { state ->
                                    watchStateSinks.remove(wss)
                                    wss.sink.success(state)
                                    wss.timeoutJob?.cancel()
                                    wss.done = true
                                }
                    }
        }
    }

    /**
     * 获取内存配置状态，如果当前不存在任何配置则返回0。
     */
    fun getMemoryLastDataTime() = lastDataTime?.time ?: 0

    /**
     * 保存应用。
     */
    fun insert(app: App): Mono<*> {
        app.id = ObjectId().toHexString()
        app.createdAt = Date()
        app.updatedAt = Date()
        return appRepository.insert(app)
    }

    /**
     * 删除应用。
     */
    fun delete(app: App, userContext: UserContext): Mono<Void> = checkPermission(app.name, app.profile, userContext).flatMap {
        appRepository.delete(app, userContext)
    }

    /**
     * 更新应用。
     */
    fun update(app: App, userContext: UserContext): Mono<Void> = checkPermission(app.name, app.profile, userContext).flatMap {
        appRepository.update(app, userContext)
    }

    /**
     * 更新应用配置。
     */
    fun updateContent(app: App, userContext: UserContext): Mono<*> {
        try {
            yaml.load<Map<String, Any>>(app.content)
        } catch (e: Exception) {
            throw BizCodeException(BizCodes.C_1006, e)
        }

        return checkPermission(app.name, app.profile, userContext).flatMap {
            appRepository.updateContent(app, userContext)
        }.doOnSuccess { dbApp ->
            Mono.defer {
                val generator = DiffRowGenerator.create()
                        .showInlineDiffs(true)
                        .mergeOriginalRevised(true)
                        .inlineDiffByWord(true)
                        .oldTag {
                            if (it) """<del style="background-color: #fdb8c0;">""" else "</del>"
                        }
                        .newTag {
                            if (it) """<span style="background-color: #acf2bd;">""" else "</span>"
                        }
                        .build()

                val rows = generator.generateDiffRows(dbApp.content.lines(), app.content.lines())
                val html = StringBuilder()
                html.apply {
                    append("<p>修改应用信息：</p>")
                    append("<ul>")
                    append("<li>应用名称（name）：").append(app.name).append("</li>")
                    append("<li>应用环境（profile）：").append(app.profile).append("</li>")
                    append("<li>修改人（updated_by）：").append(userContext.email).append("</li>")
                    append("<li>修改时间（updated_at）：").append(OffsetDateTime.now()).append("</li>")
                    append("</ul>")
                }

                html.apply {
                    append("<p>修改配置信息：</p>")
                    append("<pre>")
                    rows.forEach {
                        appendln(it.oldLine)
                    }
                    append("</pre>")
                }

                val mailer = Config.monitorEmail.smtp.run {
                    MailerBuilder.withSMTPServer(host, port, username, password).buildMailer()
                }

                val email = Config.monitorEmail.run {
                    EmailBuilder.startingBlank()
                            .from(fromAddress)
                            .to(toAddress)
                            .withSubject("${userContext.email} 修改了配置 ${app.name}/${app.profile}")
                            .withHTMLText(html.toString())
                            .buildEmail()
                }
                mailer.sendMail(email, true)

                Mono.just(dbApp)
            }.subscribeOn(Schedulers.elastic()).subscribe()
        }.flatMap { dbApp ->
            refresh().map { dbApp.v + 1 }
        }.doOnSuccess {
            // 刷新集群配置
            refreshClusterConfig()
        }
    }

    /**
     * 获取配置状态。
     */
    fun getConfigState(vo: RequestConfigVo): Mono<String> = loadAndCheckApps(vo).map { apps ->
        val state = StringBuilder()
        apps.forEach {
            state.append(it.v)
        }
        state.toString()
    }

    /**
     * 监控配置状态。
     */
    fun watchConfigState(vo: RequestConfigVo, oldState: String): Mono<String> = getConfigState(vo).flatMap { state ->
        if (state != oldState) {
            return@flatMap Mono.just(state)
        }

        if (watchRequestLimit >= 1) {
            val s = watchStateSinks.size

            // 如果监控请求达到单实例请求上限，直接拒绝该请求。
            // 收到该错误信息时，应该及时增加更多实例或者增加请求上限。
            if (s >= watchRequestLimit) {
                reliableLog.error("watch-config-state" +
                        "\n The request has reached the upper limit." +
                        "\n Adjust the number of application instances or increase the maximum request limit." +
                        "\n [watchRequestLimit={}]",
                        watchRequestLimit
                )
                throw BizCodeException(BizCodes.C_1429, "Supporting maximum request $watchRequestLimit.")
            }

            // 如果监控请求达到预设的阀值，打印警告信息提示。
            // 收到该警告信息里应该及时观察服务的可用性，在必要时应该增加更多实例或增加请求上限。
            if (s >= watchReqWarnThreshold) {
                reliableLog.warn("watch-config-state" +
                        "\n The request has reached the expected threshold." +
                        "\n Be prepared to adjust the number of application instances or increase the maximum request limit." +
                        "\n [watchRequestLimit={}, warnRateThreshold={}, watchReqWarnThreshold={}]",
                        watchRequestLimit,
                        Config.concurrent.warnRateThreshold,
                        watchReqWarnThreshold
                )
            }
        }

        Mono.create<String> { sink ->
            val wss = WatchStateSink(
                    sink = sink,
                    name = vo.name,
                    profiles = vo.profiles.toList(),
                    state = state,
                    startTime = System.currentTimeMillis()
            )

            wss.timeoutJob = GlobalScope.launch {
                delay(watchStateTimeout)
                watchStateSinks.remove(wss)
                wss.done = true

                sink.success(state)

                val endTime = System.currentTimeMillis()
                val elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(endTime - wss.startTime)
                if (elapsedSeconds > watchStateTimeout) {
                    reliableLog.warn("watch-config-state" +
                            "\n Task processing timeout: [expectedSeconds={}, elapsedSeconds={}]",
                            watchStateTimeout,
                            elapsedSeconds
                    )
                }
            }

            watchStateSinks.add(wss)
        }
    }

    /**
     * 获取兼容 `spring-cloud` 格式的配置。
     */
    fun loadSpringCloudConfig(vo: RequestConfigVo): Mono<SpringCloudResponseDto> {
        return loadAndCheckApps(vo).map { apps ->
            val ps = arrayListOf<SpringCloudPropertySource>()
            val state = StringBuilder()
            apps.forEach {
                ps.add(SpringCloudPropertySource(localKey(it.name, it.profile), flattenedMap(it.properties)))
                state.append(it.v)
            }
            SpringCloudResponseDto(name = vo.name, profiles = vo.profiles, state = state.toString(), propertySources = ps)
        }
    }

    /**
     * 获取配置。
     */
    fun loadConfigByNameProfile(vo: RequestConfigVo): Mono<Map<Any, Any?>> = loadAndCheckApps(vo).map(::mergeProps)

    /**
     * 获取某个 `key` 的具体配置。
     */
    fun loadConfigByNameProfileKey(vo: RequestConfigVo): Mono<Any> {
        return loadConfigByNameProfile(vo).map {
            var props = it
            var v: Any? = null
            for (k in vo.key.split(".")) {
                v = props[k]
                if (v == null) {
                    break
                }
                if (v is Map<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    props = v as Map<Any, Any>
                }
            }

            if (v is Map<*, *>
                    || v is Collection<*>
                    || v is Array<*>
            ) {
                return@map v
            }
            SingleValue(v)
        }
    }

    /**
     * 查询指定的应用详细信息。
     */
    fun findOne(name: String, profile: String): Mono<App> = appRepository.findOne(name, profile)
            .switchIfEmpty(Mono.defer {
                throw BizCodeException(BizCodes.C_1000, "未找到应用 $name/$profile")
            })

    /**
     * 查询所有应用详细信息。
     */
    fun findAll(): Flux<App> = appRepository.findAll()

    /**
     * 查询在指定更新时间之后的应用详细信息。
     *
     * @param updateAt 更新时间
     */
    fun findByUpdatedAt(updateAt: Date): Flux<App> = appRepository.findByUpdatedAt(updateAt)

    /**
     * 分页查询应用详细信息。
     */
    fun findPage(pageable: Pageable): Mono<Page<App>> = appRepository.findPage(pageable)

    /**
     * 分页查询用户所有的应用详细信息。
     */
    fun findPageByUser(pageable: Pageable, userContext: UserContext): Mono<Page<App>> = if (userContext.isRoot) {
        findPage(pageable)
    } else {
        appRepository.findPageByUser(pageable, userContext)
    }

    /**
     * 全文检索配置。
     */
    fun searchPageByUser(q: String, pageable: Pageable, userContext: UserContext): Mono<Page<App>> = if (userContext.isRoot) {
        appRepository.searchPage(q, pageable)
    } else {
        appRepository.searchPageByUser(q, pageable, userContext)
    }

    /**
     * 查询应用更新的最新 50 条更新记录。
     */
    fun findLast50History(name: String, profile: String, userContext: UserContext): Flux<AppContentHistory> = checkPermission(name, profile, userContext).flatMapMany {
        appRepository.findLast50History(name, profile)
    }

    /**
     * 查询所有应用名称。
     */
    fun findAllNames() = appRepository.findAllNames()

    /**
     * 查询应用下所有的环境名称。
     */
    fun findProfilesByName(name: String) = appRepository.findProfilesByName(name)

    /**
     * 返回数据库最新配置修改时间，如果不存在配置则返回0。
     */
    fun findLastDataTime() = appRepository.findLastDataTime()

    private fun checkPermission(name: String, profile: String, userContext: UserContext): Mono<Unit> {
        if (userContext.isRoot) {
            return Mono.just(Unit)
        }

        return appRepository.findOne(name, profile).flatMap {
            if (!it.users.contains(userContext.email)) {
                // 用户没有修改该 APP 的权限
                throw BizCodeException(BizCode.Classic.C_403)
            }

            Mono.just(Unit)
        }
    }

    internal fun loadAndCheckApps(vo: RequestConfigVo): Mono<List<CachedApp>> = Flux.merge(vo.profiles.map {
        loadOne(vo.name, it).doOnNext { app ->
            if (app.ipLimit.isNotEmpty()) {
                // 校验访问 IP
                if (vo.clientIpv4.isEmpty()) {
                    throw BizCodeException(BizCode.Classic.C_403, "${app.name}/${app.profile} 禁止访问")
                }

                val ipl = IpUtils.ipv4ToLong(vo.clientIpv4)
                var r = false
                for (limit in app.ipLimit) {
                    r = limit.check(ipl)
                    if (r) {
                        break
                    }
                }

                if (!r) {
                    throw BizCodeException(BizCode.Classic.C_403, "${app.name}/${app.profile} 禁止 ${vo.clientIpv4} 访问")
                }
            }

            if (app.token.isNotEmpty() && !vo.configTokens.contains(app.token)) {
                throw BizCodeException(BizCode.Classic.C_401, "${app.name}/${app.profile} 认证失败")
            }
        }
    }).collectList()

    internal fun loadOne(name: String, profile: String): Mono<CachedApp> {
        val k = localKey(name, profile)
        return Mono.justOrEmpty<CachedApp>(appCaches.getIfPresent(k))
                .switchIfEmpty(
                        findOne(name, profile).map {
                            val c = mapToCachedApp(it)
                            appCaches.put(k, c)
                            c
                        }.switchIfEmpty(Mono.create {
                            it.error(BizCodeException(BizCodes.C_1000, "未找到应用 $name/$profile"))
                        })
                )
    }

    private fun configState0(name: String, profiles: List<String>): Mono<String> {
        val sources = profiles.map { loadOne(name, it) }
        return Flux.concat(sources).collectList().map { apps ->
            val state = StringBuilder()
            apps.forEach { app ->
                state.append(app.v)
            }
            state.toString()
        }
    }

    private fun mergeProps(apps: List<CachedApp>): Map<Any, Any> {
        if (apps.size == 1) {
            return apps.first().properties
        }

        val m = linkedMapOf<Any, Any>()
        apps.forEach {
            mergeProps(m, it.properties.toMutableMap())
        }
        return m
    }

    @Suppress("UNCHECKED_CAST")
    private fun mergeProps(a: MutableMap<Any, Any>, b: MutableMap<Any, Any>, prefix: String = "") {
        if (a.isEmpty()) {
            a.putAll(b)
            return
        }

        b.forEach { (k, v) ->
            if (v is Map<*, *>) {
                val field: String = if (prefix.isEmpty()) k.toString() else "$prefix.$k"

                val o = a[k]
                if (o != null && o !is Map<*, *>) {
                    throw BizCodeException(BizCodes.C_1005, "[$field] 数据类型为 [${o.javaClass}] 不能与 [Object] 合并")
                }

                val m = (o as? Map<Any, Any>)?.toMutableMap() ?: hashMapOf()
                mergeProps(m, v.toMutableMap() as MutableMap<Any, Any>, field)
                a[k] = m
            } else {
                a[k] = v
            }
        }
    }

    private fun flattenedMap(source: Map<Any, Any>): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        buildFlattenedMap(result, source, "")
        return result
    }

    private fun buildFlattenedMap(result: MutableMap<String, Any>, source: Map<Any, Any>, path: String) {
        for (entry in source.entries) {
            var key: String = entry.key as? String ?: entry.key.toString()

            if (path.isNotEmpty()) {
                key = if (key.startsWith("[")) {
                    path + key
                } else {
                    "$path.$key"
                }
            }

            when (val value = entry.value) {
                is Map<*, *> -> {
                    // Need a compound key
                    val map = value as Map<Any, Any>
                    buildFlattenedMap(result, map, key)
                }
                is Collection<*> -> {
                    // Need a compound key
                    val collection = value as Collection<Any>
                    for ((count, o) in collection.withIndex()) {
                        buildFlattenedMap(result, mapOf("[$count]" to o), key)
                    }
                }
                else -> result[key] = value
            }
        }
    }

    private fun mapToCachedApp(app: App): CachedApp {
        val ipLimit = if (app.ipLimit.isNullOrEmpty()) {
            emptyList<IpChecker>()
        } else {
            val list = arrayListOf<IpChecker>()
            app.ipLimit.split(",").forEach {
                if (it.contains("-")) {
                    val a = it.split("-")
                    val from = IpUtils.ipv4ToLong(a.first())
                    val to = IpUtils.ipv4ToLong(a.last())
                    list.add(SectionIpChecker(from, to))
                } else {
                    list.add(SingleIpChecker(IpUtils.ipv4ToLong(it)))
                }
            }
            list
        }

        return CachedApp(
                name = app.name,
                profile = app.profile,
                token = app.token,
                v = app.v,
                ipLimit = ipLimit,
                properties = if (app.content.isNotEmpty()) yaml.load(app.content) else emptyMap()
        )
    }

    private fun localKey(name: String, profile: String) = "${name}_$profile"

    // 刷新集群配置
    private fun refreshClusterConfig() {
        serverRepository.findServers().subscribe { s ->
            val server = Config.server
            // 跳过本机
            if (s.host == server.host && s.port == server.port) {
                return@subscribe
            }

            val schema = if (server.sslEnabled) "https" else "http"
            webClientBuilder
                    .baseUrl("$schema://${s.host}:${s.port}")
                    .build()
                    .post()
                    .uri("/servers/apps/refresh")
                    .accept(APPLICATION_JSON)
                    .retrieve()
                    .onStatus({ !it.is2xxSuccessful }, {
                        Mono.error(IllegalStateException("http status: ${it.statusCode().value()}"))
                    })
                    .bodyToMono(ServerRefreshDto::class.java)
                    .handle<ServerRefreshDto> { t, u ->
                        val luat = lastDataTime!!.time
                        if (t.lastDataTime != luat) {
                            u.error(IllegalStateException("${s.host}:${s.port} 刷新最终更新时间不一致 local-lastDataTime: $luat, remote-lastDataTime: ${t.lastDataTime}"))
                        }
                    }
                    .retry(3)
                    .doOnError {
                        log.error("${s.host}:${s.port} 刷新失败", it)
                    }
                    .subscribe()
        }
    }

}