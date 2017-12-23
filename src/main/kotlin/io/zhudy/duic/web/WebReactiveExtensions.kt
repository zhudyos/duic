package io.zhudy.duic.web

import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
private val path = "path"
private val query = "query"

/**
 * 返回 `path` 参数.
 *
 * `boolean` 取值设定.
 *
 * - true/false
 * - not 0/0
 * - on/off
 *
 * @param name 参数名称
 */
fun ServerRequest.pathBoolean(name: String) = requestBooleanParam(this, path, name)

/**
 * 返回 `path` 参数.
 *
 * @param name 参数名称
 */
fun ServerRequest.pathInt(name: String) = requestIntParam(this, path, name)

/**
 * 返回 `path` 参数.
 *
 * @param name 参数名称
 */
fun ServerRequest.pathLong(name: String) = requestLongParam(this, path, name)

/**
 * 返回 `path` 参数.
 *
 * @param name 参数名称
 */
fun ServerRequest.pathDouble(name: String) = requestDoubleParam(this, path, name)

/**
 * 返回 `path` 参数.
 *
 * @param name 参数名称
 */
fun ServerRequest.pathString(name: String) = requestStringParam(this, path, name)

/**
 * 返回 `path` 参数并去除前后空格.
 *
 * @param name 参数名称
 */
fun ServerRequest.pathTrimString(name: String) = requestTrimStringParam(this, path, name)

/**
 * 返回 `query` 参数.
 *
 * `boolean` 取值设定.
 *
 * - true/false
 * - not 0/0
 * - on/off
 *
 * @param name 参数名称
 */
fun ServerRequest.queryBoolean(name: String) = requestBooleanParam(this, query, name)

/**
 * 返回 `query` 参数.
 *
 * @param name 参数名称
 */
fun ServerRequest.queryInt(name: String) = requestIntParam(this, query, name)

/**
 * 返回 `query` 参数.
 *
 * @param name 参数名称
 */
fun ServerRequest.queryLong(name: String) = requestLongParam(this, query, name)

/**
 * 返回 `query` 参数.
 *
 * @param name 参数名称
 */
fun ServerRequest.queryDouble(name: String) = requestDoubleParam(this, query, name)

/**
 * 返回 `query` 参数.
 *
 * @param name 参数名称
 */
fun ServerRequest.queryString(name: String) = requestStringParam(this, query, name)

/**
 * 返回 `query` 参数并去除前后空格.
 *
 * @param name 参数名称
 */
fun ServerRequest.queryTrimString(name: String) = requestTrimStringParam(this, query, name)

// =================================================================================================================

/**
 *
 */
fun ServerResponse.BodyBuilder.body(o: Any): Mono<ServerResponse> = body(BodyInserters.fromObject(o))


// =================================================================================================================
private fun requestBooleanParam(request: ServerRequest, where: String, name: String): Boolean {
    val v = requestTrimStringParam(request, where, name).toLowerCase()
    return v == "true" || v != "0" || v == "on"
}

private fun requestIntParam(request: ServerRequest, where: String, name: String): Int {
    val v = requestTrimStringParam(request, where, name)
    try {
        return v.toInt()
    } catch (e: NumberFormatException) {
        throw RequestParameterFormatException(where, name, """无法将 "$v" 转换为 int 类型""")
    }
}

private fun requestLongParam(request: ServerRequest, where: String, name: String): Long {
    val v = requestTrimStringParam(request, where, name)
    try {
        return v.toLong()
    } catch (e: NumberFormatException) {
        throw RequestParameterFormatException(where, name, """无法将 "$v" 转换为 long 类型""")
    }
}

private fun requestDoubleParam(request: ServerRequest, where: String, name: String): Double {
    val v = requestTrimStringParam(request, where, name)
    try {
        return v.toDouble()
    } catch (e: NumberFormatException) {
        throw RequestParameterFormatException(where, name, """无法将 "$v" 转换为 double 类型""")
    }
}

private fun requestStringParam(request: ServerRequest, where: String, name: String): String {
    val v = requestParam(request, where, name)
    if (v == null || v.isEmpty()) {
        throw MissingRequestParameterException(where, name)
    }
    return v
}

private fun requestTrimStringParam(request: ServerRequest, where: String, name: String): String {
    val v = requestParam(request, where, name)?.trim()
    if (v == null || v.isEmpty()) {
        throw MissingRequestParameterException(where, name)
    }
    return v
}

private fun requestParam(request: ServerRequest, where: String, name: String): String? = if (path == where) request.pathVariable(name) else request.queryParam(name).orElse(null)
