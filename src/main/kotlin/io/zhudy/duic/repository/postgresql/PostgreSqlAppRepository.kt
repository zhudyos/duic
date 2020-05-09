package io.zhudy.duic.repository.postgresql

import io.zhudy.duic.domain.App
import io.zhudy.duic.repository.AbstractAppRepository
import io.zhudy.duic.vo.AppVo
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.query.Criteria
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class PostgreSqlAppRepository(
        private val dc: DatabaseClient
) : AbstractAppRepository(dc) {

    companion object {
        private const val NEXT_GV_SQL = "select nextval('duic_app_gv_seq')"
    }

    override fun nextGv(): Mono<Long> = Mono.defer {
        dc.execute(NEXT_GV_SQL)
                .map { row -> row[0] as Long }
                .one()
    }

    override fun search(vo: AppVo.UserQuery): Flux<App> = Flux.defer {
        val filters = ArrayList<Criteria>(2)
        if (vo.q != null) {
            val s = "%${vo.q}%"
            filters.add(Criteria.where("name").like(s).or("profile").like(s).or("description").like(s).or("content").like(s))
        }
        if (vo.email != null) {
            filters.add(Criteria.where("email").like("%${vo.email}%"))
        }

        dc.select().from("DUIC_APP").matching(Criteria.from(filters))
                .map { row -> mapToApp(row) }
                .all()
    }

}