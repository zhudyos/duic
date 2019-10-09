package io.zhudy.duic.repository

import io.zhudy.duic.domain.*
import io.zhudy.duic.dto.AppQueryDto
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
        private const val FIND_ONE_SQL = "SELECT * FROM DUIC_APP WHERE NAME=:name AND PROFILE=:profile"
        private const val UPDATE_SQL = "UPDATE DUIC_APP SET token=:token,ip_limit=:ipLimit,description=:description,users=:users,updated_at=now() WHERE name=:name AND profile=:profile AND v=:v"
        private const val UPDATE_CONTENT_SQL = "UPDATE DUIC_APP SET content=:content,v=v+1,updated_at=now() WHERE name=:name AND profile=:profile AND v=:v"
        private const val FIND_ALL_NAMES = "SELECT name FROM DUIC_APP"
        private const val FIND_PROFILES_BY_NAME = "SELECT profile FROM DUIC_APP WHERE name=:name"
        private const val FIND_LAST_DATA_TIME_SQL = "SELECT updated_at FROM DUIC_APP ORDER BY updated_at DESC"
    }

    override fun insert(app: App): Mono<Int> = Mono.defer {
        dc.execute(INSERT_SQL)
                .bind("name", app.name)
                .bind("profile", app.profile)
                .bind("description", app.description)
                .bind("token", app.token)
                .bind("ipLimit", app.ipLimit)
                .bind("content", app.content)
                .bind("v", app.v)
                .bind("users", app.users.joinToString(","))
                .fetch()
                .rowsUpdated()
    }

    override fun delete(app: App): Mono<Int> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findOne(name: String, profile: String): Mono<App> = Mono.defer {
        dc.execute(FIND_ONE_SQL)
                .bind("name", name)
                .bind("profile", profile)
                .`as`(App::class.java)
                .fetch()
                .one()
    }

    override fun update(app: App): Mono<Int> = Mono.defer {
        dc.execute(UPDATE_SQL)
                .bind("token", app.token)
                .bind("ipLimit", app.ipLimit)
                .bind("description", app.description)
                .bind("users", app.users.joinToString(","))
                .bind("name", app.name)
                .bind("profile", app.profile)
                .bind("v", app.v)
                .fetch()
                .rowsUpdated()
    }

    override fun updateContent(name: String, profile: String, content: String): Mono<Int> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findAll(): Flux<App> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun searchPage(query: AppQueryDto, pageable: Pageable): Mono<Page<App>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findLast50History(name: String, profile: String): Flux<AppContentHistory> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findAllNames(): Flux<String> = Flux.defer {
        dc.execute(FIND_ALL_NAMES)
                .map { row -> row.get(1, String::class.java) }
                .all()
    }

    override fun findProfilesByName(name: String): Flux<String> = Flux.defer {
        dc.execute(FIND_PROFILES_BY_NAME)
                .map { row -> row.get(1, String::class.java) }
                .all()
    }

    override fun findDeletedByCreatedAt(createdAt: Instant): Flux<AppHistory> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findLastDataTime(): Mono<Long> = Mono.defer {
        dc.execute(FIND_LAST_DATA_TIME_SQL)
                .map { row -> row.get(1, Long::class.java) }
                .first()
                .defaultIfEmpty(0)
    }
}