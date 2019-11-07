package io.zhudy.duic.repository

import io.r2dbc.spi.Row
import io.zhudy.duic.domain.App
import io.zhudy.duic.domain.AppContentHistory
import io.zhudy.duic.domain.AppHistory
import io.zhudy.duic.domain.AppPair
import io.zhudy.duic.vo.AppVo
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.core.DatabaseClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
abstract class AbstractRelationalAppRepository(
        private val dc: DatabaseClient
) : AppRepository {

    companion object {
        private const val INSERT_SQL = "INSERT INTO DUIC_APP(name,profile,description,token,ip_limit,content,v,users,updated_at) VALUES(:name,:profile,:description,:token,:ipLimit,:content,:v,:users,NOW())"
        private const val DELETE_SQL = "DELETE FROM DUIC_APP WHERE name=:name AND profile=:profile"
        private const val FIND_ONE_SQL = "SELECT * FROM DUIC_APP WHERE name=:name AND profile=:profile"
        private const val UPDATE_SQL = "UPDATE DUIC_APP SET token=:token,ip_limit=:ipLimit,description=:description,users=:users,updated_at=now() WHERE name=:name AND profile=:profile AND v=:v"
        private const val UPDATE_CONTENT_SQL = "UPDATE DUIC_APP SET content=:content,v=v+1,updated_at=now() WHERE name=:name AND profile=:profile AND v=:v"
        private const val FIND_ALL_SQL = "SELECT * FROM DUIC_APP"
        private const val FIND_ALL_NAMES = "SELECT name FROM DUIC_APP"
        private const val FIND_PROFILES_BY_NAME = "SELECT profile FROM DUIC_APP WHERE name=:name"
        private const val FIND_LAST_DATA_TIME_SQL = "SELECT updated_at FROM DUIC_APP ORDER BY updated_at DESC"
    }

    override fun insert(vo: AppVo.NewApp): Mono<Int> = Mono.defer {
        dc.execute(INSERT_SQL)
                .bind("name", vo.name)
                .bind("profile", vo.profile)
                .bind("description", vo.description)
                .bind("token", vo.token)
                .bind("ipLimit", vo.ipLimit)
                .bind("content", vo.content)
                .bind("v", 1)
                .bind("users", vo.users.joinToString(","))
                .fetch()
                .rowsUpdated()
    }

    override fun delete(ap: AppPair): Mono<Int> = Mono.defer {
        dc.execute(DELETE_SQL)
                .bind("name", ap.name)
                .bind("profile", ap.profile)
                .fetch()
                .rowsUpdated()
    }

    override fun findOne(ap: AppPair): Mono<App> = Mono.defer {
        dc.execute(FIND_ONE_SQL)
                .bind("name", ap.name)
                .bind("profile", ap.profile)
                .map(::mapToApp)
                .one()
    }

    override fun update(ap: AppPair, vo: AppVo.UpdateBasicInfo): Mono<Int> = Mono.defer {
        dc.execute(UPDATE_SQL)
                .bind("token", vo.token)
                .bind("ipLimit", vo.ipLimit)
                .bind("description", vo.description)
                .bind("users", vo.users.joinToString(","))
                .bind("name", ap.name)
                .bind("profile", ap.profile)
                .bind("v", vo.v)
                .fetch()
                .rowsUpdated()
    }

    override fun updateContent(ap: AppPair, vo: AppVo.UpdateContent): Mono<Int> = Mono.defer {
        dc.execute(UPDATE_CONTENT_SQL)
                .bind("name", ap.name)
                .bind("profile", ap.profile)
                .bind("v", vo.v)
                .bind("content", vo.content)
                .fetch()
                .rowsUpdated()
    }

    override fun findAll(): Flux<App> = Flux.defer {
        dc.execute(FIND_ALL_SQL)
                .map(::mapToApp)
                .all()
    }

    override fun search(vo: AppVo.UserQuery, pageable: Pageable): Mono<Page<App>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findAppHistory(ap: AppPair, pageable: Pageable): Flux<AppContentHistory> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findAllNames(): Flux<String> = Flux.defer {
        dc.execute(FIND_ALL_NAMES)
                .map { row -> row.get(1, String::class.java) }
                .all()
    }

    override fun findProfilesByName(name: String): Flux<String> = Flux.defer {
        dc.execute(FIND_PROFILES_BY_NAME)
                .bind("name", name)
                .map { row -> row.get(1, String::class.java) }
                .all()
    }

    override fun findLatestDeleted(time: Instant): Flux<AppHistory> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findLastDataTime(): Mono<Long> = Mono.defer {
        dc.execute(FIND_LAST_DATA_TIME_SQL)
                .map { row -> row.get(1, Long::class.java) }
                .first()
                .defaultIfEmpty(0)
    }

    private fun mapToApp(row: Row) = App(
            name = row.get("name", String::class.java),
            profile = row.get("profile", String::class.java),
            description = row.get("description", String::class.java),
            content = row.get("content", String::class.java),
            token = row.get("token", String::class.java),
            ipLimit = row.get("ip_limit", String::class.java),
            v = row.get("v", Int::class.java),
            users = row.get("users", String::class.java).split(","),
            createdAt = row.get("created_at", Instant::class.java),
            updatedAt = row.get("updated_at", Instant::class.java)
    )
}