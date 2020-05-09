package io.zhudy.duic.repository.mysql

import io.zhudy.duic.domain.App
import io.zhudy.duic.repository.AbstractAppRepository
import io.zhudy.duic.vo.AppVo
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.query.Criteria
import org.springframework.data.r2dbc.query.Criteria.from
import org.springframework.data.r2dbc.query.Criteria.where
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class MySqlAppRepository(
        private val dc: DatabaseClient
) : AbstractAppRepository(dc) {

    companion object {
        private const val NEXT_GV_SQL = "update duic_app_gv_seq set id=last_insert_id(id + 1); select last_insert_id();"
    }

    override fun nextGv(): Mono<Long> = Mono.defer {
        dc.execute(NEXT_GV_SQL)
                .map { row -> row.get(0, Long::class.java) }
                .one()
    }

    override fun search(vo: AppVo.UserQuery): Flux<App> = Flux.defer {
        val filters = ArrayList<Criteria>(2)
        if (vo.q != null) {
            val s = "%${vo.q}%"
            filters.add(where("name").like(s).or("profile").like(s).or("description").like(s).or("content").like(s))
        }
        if (vo.email != null) {
            filters.add(where("email").like("%${vo.email}%"))
        }

        dc.select().from("DUIC_APP").matching(from(filters))
                .map { row -> mapToApp(row) }
                .all()
    }
}