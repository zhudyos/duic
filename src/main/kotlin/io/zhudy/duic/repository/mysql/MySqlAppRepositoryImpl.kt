package io.zhudy.duic.repository.mysql

import io.zhudy.duic.domain.App
import io.zhudy.duic.repository.AbstractRelationalAppRepository
import io.zhudy.duic.vo.AppVo
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.core.DatabaseClient
import reactor.core.publisher.Mono

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class MySqlAppRepositoryImpl(
        private val dc: DatabaseClient
) : AbstractRelationalAppRepository(dc) {

    override fun search(vo: AppVo.UserQuery, pageable: Pageable): Mono<Page<App>> = Mono.defer {
        val searchSql = StringBuilder("SELECT * FROM DUIC_APP WHERE 1=1")
        val searchCountSql = StringBuilder("SELECT COUNT(*) FROM DUIC_APP WHERE 1=1")
        val params = mutableListOf<Pair<String, Any>>()
        if (vo.q != null) {
            searchSql.append(" AND MATCH(name, profile, content) AGAINST(:q)")
            searchCountSql.append(" AND MATCH(name, profile, content) AGAINST(:q)")
            params.add("q" to vo.q)
        }
        if (vo.email != null) {
            searchSql.append(" AND users LIKE CONCAT('%', :email, '%')")
            searchCountSql.append(" AND users LIKE CONCAT('%', :email, '%')")
            params.add("email" to vo.email)
        }
        searchSql.append(" LIMIT :offset,:limit")

        // query
        var spec = dc.execute(searchSql.toString())
                .bind("offset", pageable.offset)
                .bind("limit", pageable.pageSize)

        // count
        var specCount = dc.execute(searchCountSql.toString())
        if (params.isNotEmpty()) {
            for (o in params) {
                spec = spec.bind(o.first, o.second)
                specCount = specCount.bind(o.first, o.second)
            }
        }

        spec.map(::mapToApp).all().collectList()
                .zipWith(
                        specCount.map { row -> row.get(0, Long::class.java) }.one()
                )
                .map {
                    PageImpl(it.t1, pageable, it.t2)
                }
    }
}