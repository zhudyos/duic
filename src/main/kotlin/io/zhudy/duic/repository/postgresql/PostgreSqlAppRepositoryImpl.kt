package io.zhudy.duic.repository.postgresql

import io.zhudy.duic.domain.App
import io.zhudy.duic.repository.AbstractAppRepository
import io.zhudy.duic.vo.AppVo
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.core.DatabaseClient
import reactor.core.publisher.Mono

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class PostgreSqlAppRepositoryImpl(
        private val dc: DatabaseClient
) : AbstractAppRepository(dc) {

    override fun search(vo: AppVo.UserQuery, pageable: Pageable): Mono<Page<App>> = Mono.defer {
        val sql = StringBuilder("SELECT * FROM DUIC_APP WHERE 1=1")
        val countSql = StringBuilder("SELECT COUNT(*) FROM DUIC_APP WHERE 1=1")

        val params = mutableListOf<Pair<String, Any>>()
        val where = StringBuilder()
        if (vo.q != null) {
            where.append(" AND TO_TSVECTOR(name || ' ' || profile || ' ' || content) @@ TO_TSQUERY(:q)")
            params.add("q" to vo.q)
        }
        if (vo.email != null) {
            where.append(" AND users LIKE CONCAT('%', :email, '%')")
            params.add("email" to vo.email)
        }

        sql.append(where).append(" LIMIT :limit OFFSET :offset")
        countSql.append(where)

        // query
        var spec = dc.execute(sql.toString())
                .bind("offset", pageable.offset)
                .bind("limit", pageable.pageSize)

        // count
        var specCount = dc.execute(countSql.toString())
        if (params.isNotEmpty()) {
            for (o in params) {
                spec = spec.bind(o.first, o.second)
                specCount = specCount.bind(o.first, o.second)
            }
        }

        spec.map(::mapToApp).all().collectList()
                .zipWith(
                        specCount.map { row -> row[0] as Long }.one()
                )
                .map {
                    PageImpl(it.t1, pageable, it.t2)
                }
    }

}