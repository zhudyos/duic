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
package io.zhudy.duic.service

import io.zhudy.duic.*
import io.zhudy.duic.domain.App
import io.zhudy.duic.domain.Page
import io.zhudy.duic.domain.Pageable
import io.zhudy.duic.domain.SingleValue
import io.zhudy.duic.dto.ServerRefreshDto
import io.zhudy.duic.dto.SpringCloudPropertySource
import io.zhudy.duic.dto.SpringCloudResponseDto
import io.zhudy.duic.repository.AppRepository
import io.zhudy.duic.repository.ServerRepository
import io.zhudy.duic.service.ip.SectionIpChecker
import io.zhudy.duic.service.ip.SingleIpChecker
import io.zhudy.duic.utils.IpUtils
import io.zhudy.duic.vo.RequestConfigVo
import org.bson.types.ObjectId
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.yaml.snakeyaml.Yaml
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

/**
 * 应用配置逻辑处理实现。
 *
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Service
class AppService(
        val appRepository: AppRepository,
        val serverRepository: ServerRepository,
        val webClientBuilder: WebClient.Builder
) {

    private val log = LoggerFactory.getLogger(AppService::class.java)

    private val appCaches = ConcurrentHashMap<String, CachedApp>()

    private val watchStateMonoSinkTimeout = 30 * 1000
    private val watchStateLock = ReentrantLock()
    private val watchStateCond = watchStateLock.newCondition()
    private val watchStateMonoSinks = ConcurrentHashMap<String, MutableList<WatchStateMonoSink>>()
    private val updateAppQueue = LinkedBlockingQueue<String>()
    private var minWatchStateMs = 0L

    @Volatile
    private var lastUpdatedAt: Date? = null
    @Volatile
    private var lastAppHistoryCreatedAt = Date()
    private val yaml = Yaml()

    /**
     * 缓存的 APP 实例。
     */
    private data class CachedApp(
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
    private data class WatchStateMonoSink(
            val monoSink: MonoSink<String>,
            val configVo: RequestConfigVo,
            val state: String,
            val initTime: Long
    )

    init {
        initResponseWatchState()
    }

    @Scheduled(initialDelay = 0,
            fixedDelayString = "%{duic.app.watch.updated.fixed_delay:%{duic.app.watch.updated.fixed-delay:%{duic.app.watch.updated.fixedDelay:60000}}}")
    fun watchApps() {
        refresh().subscribe()
    }

    @Scheduled(initialDelay = 0,
            fixedDelayString = "%{duic.app.watch.deleted.fixed_delay:%{duic.app.watch.deleted.fixed-delay:%{duic.app.watch.deleted.fixedDelay:600000}}}")
    fun watchDeletedApps() {
        // 清理已经删除的 APP
        appRepository.findDeletedByCreatedAt(lastAppHistoryCreatedAt).doOnError {
            log.error("evict app config: ", it)
        }.doFinally {
            log.debug("lastAppHistoryCreatedAt={}", lastAppHistoryCreatedAt.time)
        }.subscribe {
            appCaches.remove(localKey(it.name, it.profile))
            lastAppHistoryCreatedAt = it.createdAt!!.toDate()
        }
    }

    /**
     * 刷新内存的配置信息。
     *
     * @return 配置最后的更新时间戳（毫秒）
     */
    fun refresh() = Mono.create<Long> { sink ->
        // 更新 APP 配置信息
        if (lastUpdatedAt == null) {
            findAll()
        } else {
            findByUpdatedAt(lastUpdatedAt!!)
        }.doOnError {
            log.error("refresh app config: ", it)
        }.doFinally {
            log.debug("lastUpdatedAt={}", lastUpdatedAt?.time)
        }.doOnComplete {
            sink.success(lastUpdatedAt?.time ?: 0L)
        }.subscribe {
            val k = localKey(it.name, it.profile)
            appCaches.put(k, mapToCachedApp(it))
            lastUpdatedAt = it.updatedAt!!.toDate()

            updateAppQueue.offer(k)
        }
    }

    /**
     * 保存应用。
     */
    fun insert(app: App): Mono<*> {
        app.id = ObjectId().toHexString()
        app.createdAt = DateTime.now()
        app.updatedAt = DateTime.now()
        return appRepository.insert(app)
    }

    /**
     * 删除应用。
     */
    fun delete(app: App, userContext: UserContext) = checkPermission(app.name, app.profile, userContext).flatMap {
        appRepository.delete(app, userContext)
    }

    /**
     * 更新应用。
     */
    fun update(app: App, userContext: UserContext) = checkPermission(app.name, app.profile, userContext).flatMap {
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
        }.flatMap { v ->
            refresh().map {
                v
            }
        }.doOnSuccess {
            // 刷新集群配置
            refreshClusterConfig()
        }
    }

    /**
     * 获取配置状态。
     */
    fun getConfigState(vo: RequestConfigVo): Mono<String> {
        return loadAndCheckApps(vo).map { apps ->
            val state = StringBuilder()
            apps.forEach {
                state.append(it.v)
            }
            state.toString()
        }
    }

    /**
     * 监控配置状态。
     */
    fun watchConfigState(vo: RequestConfigVo, oldState: String) = getConfigState(vo).flatMap { state ->
        if (state != oldState) {
            return@flatMap Mono.just(state)
        }
        Mono.create<String> { sink ->
            val ws = WatchStateMonoSink(sink, vo, state, System.currentTimeMillis())

            watchStateLock.withLock {
                vo.profiles.forEach {
                    val k = localKey(vo.name, it)
                    val a = watchStateMonoSinks[k] ?: mutableListOf()
                    a.add(ws)

                    watchStateMonoSinks[k] = a
                }

                if (minWatchStateMs == 0L) {
                    minWatchStateMs = ws.initTime
                    watchStateCond.signalAll()
                }
            }
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
    fun loadConfigByNameProfile(vo: RequestConfigVo) = loadAndCheckApps(vo).map(::mergeProps)

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
    fun findOne(name: String, profile: String) = appRepository.findOne<Any>(name, profile)

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
    fun findLast50History(name: String, profile: String, userContext: UserContext)
            = checkPermission(name, profile, userContext).flatMapMany {
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

    private fun checkPermission(name: String, profile: String, userContext: UserContext): Mono<Unit> {
        if (userContext.isRoot) {
            return Mono.just(Unit)
        }

        return appRepository.findOne<Any>(name, profile).flatMap {
            if (!it.users.contains(userContext.email)) {
                // 用户没有修改该 APP 的权限
                throw BizCodeException(BizCode.Classic.C_403)
            }

            Mono.just(Unit)
        }
    }

    private fun loadAndCheckApps(vo: RequestConfigVo) = Flux.merge(vo.profiles.map {
        loadOne(vo.name, it).doOnNext {
            if (it.ipLimit.isNotEmpty()) {
                // 校验访问 IP
                if (vo.clientIpv4.isEmpty()) {
                    throw BizCodeException(BizCode.Classic.C_403, "${it.name}/${it.profile} 禁止访问")
                }

                val ipl = IpUtils.ipv4ToLong(vo.clientIpv4)
                var r = false
                for (limit in it.ipLimit) {
                    r = limit.check(ipl)
                    if (r) {
                        break
                    }
                }

                if (!r) {
                    throw BizCodeException(BizCode.Classic.C_403, "${it.name}/${it.profile} 禁止 ${vo.clientIpv4} 访问")
                }
            }

            if (it.token.isNotEmpty() && !vo.configTokens.contains(it.token)) {
                throw BizCodeException(BizCode.Classic.C_401, "${it.name}/${it.profile} 认证失败")
            }
        }
    }).collectList()

    private fun loadOne(name: String, profile: String): Mono<CachedApp> {
        val k = localKey(name, profile)
        return Mono.justOrEmpty<CachedApp>(appCaches[k])
                .switchIfEmpty(
                        findOne(name, profile).map {
                            val ca = mapToCachedApp(it)
                            appCaches.put(k, ca)
                            ca
                        }.switchIfEmpty(Mono.create {
                            it.error(BizCodeException(BizCodes.C_1000, "未找到应用 $name/$profile"))
                        })
                )
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

        b.forEach { k, v ->
            if (v is Map<*, *>) {
                val field: String = if (prefix.isEmpty()) k.toString() else "$prefix.$k"

                val o = a[k]
                if (o != null && o !is Map<*, *>) {
                    throw BizCodeException(BizCodes.C_1005, "[$field] 数据类型为 [${o.javaClass}] 不能与 [Object] 合并")
                }

                val m = (o as? Map<Any, Any>)?.toMutableMap() ?: hashMapOf()
                mergeProps(m, v.toMutableMap() as MutableMap<Any, Any>, field)
                a.put(k, m)
            } else {
                a.put(k, v)
            }
        }
    }

    private fun flattenedMap(source: Map<Any, Any>): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        buildFlattenedMap(result, source, "")
        return result
    }

    @Suppress("UNCHECKED_CAST")
    private fun buildFlattenedMap(result: MutableMap<String, Any>, source: Map<Any, Any>, path: String) {
        for (entry in source.entries) {
            var key: String = entry.key as? String ?: entry.key.toString()

            if (path.isNotEmpty()) {
                key = if (key.startsWith("[")) {
                    path + key
                } else {
                    path + '.' + key
                }
            }

            val value = entry.value
            when (value) {
                is String -> result.put(key, value)
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
                else -> result.put(key, value)
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
                properties = if (!app.content.isEmpty()) yaml.load(app.content) else emptyMap()
        )
    }

    private fun localKey(name: String, profile: String) = "${name}_$profile"

    private fun initResponseWatchState() {
        thread(isDaemon = true, name = "response-watch-app-state") {
            while (true) {
                try {
                    val k = updateAppQueue.take()
                    val a = watchStateLock.withLock {
                        watchStateMonoSinks.remove(k)
                    }

                    a?.forEach { stateMonoSink ->
                        try {
                            getConfigState(stateMonoSink.configVo).subscribe {
                                stateMonoSink.monoSink.success(it)
                            }
                        } catch (e: Exception) {
                            stateMonoSink.monoSink.error(e)
                        }
                    }
                } catch (e: Exception) {
                    log.error("通知更新状态错误", e)
                }
            }
        }

        thread(isDaemon = true, name = "default-response-watch-app-state") {
            while (true) {
                try {
                    watchStateLock.withLock {

                        if (minWatchStateMs != 0L) {
                            watchStateCond.await(System.currentTimeMillis() - minWatchStateMs, TimeUnit.MILLISECONDS)
                        } else {
                            watchStateCond.await()
                        }

                        var t = 0L

                        watchStateMonoSinks.entries.forEach { entry ->
                            val mlist = entry.value

                            mlist.toList().forEach {
                                val to = it.initTime + watchStateMonoSinkTimeout
                                if (to <= System.currentTimeMillis()) {
                                    it.monoSink.success(it.state)

                                    // 清理
                                    if (mlist.size <= 1) {
                                        watchStateMonoSinks.remove(entry.key)
                                    } else {
                                        mlist.remove(it)
                                    }
                                } else {
                                    if (t == 0L || it.initTime < t) {
                                        t = it.initTime
                                    }
                                }
                            }
                        }

                        minWatchStateMs = t
                    }
                } catch (e: Exception) {
                    log.error("默认状态响应错误", e)
                }
            }
        }
    }

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
                        val luat = lastUpdatedAt!!.time
                        if (t.lastUpdatedAt != luat) {
                            u.error(IllegalStateException("${s.host}:${s.port} 刷新最终更新时间不一致 local-lastUpdatedAt: $luat, remote-lastUpdatedAt: ${t.lastUpdatedAt}"))
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