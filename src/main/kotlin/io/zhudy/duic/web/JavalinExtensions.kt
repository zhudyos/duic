package io.zhudy.duic.web

import io.javalin.Context

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
private val path = "path"
private val query = "query"
private val form = "form"

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
fun Context.pathBoolean(name: String) = requestBooleanParam(this, path, name)

/**
 * 返回 `path` 参数.
 *
 * @param name 参数名称
 */
fun Context.pathInt(name: String) = requestIntParam(this, path, name)

/**
 * 返回 `path` 参数.
 *
 * @param name 参数名称
 */
fun Context.pathLong(name: String) = requestLongParam(this, path, name)

/**
 * 返回 `path` 参数.
 *
 * @param name 参数名称
 */
fun Context.pathDouble(name: String) = requestDoubleParam(this, path, name)

/**
 * 返回 `path` 参数.
 *
 * @param name 参数名称
 */
fun Context.pathString(name: String) = requestStringParam(this, path, name)

/**
 * 返回 `path` 参数并去除前后空格.
 *
 * @param name 参数名称
 */
fun Context.pathTrimString(name: String) = requestTrimStringParam(this, path, name)

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
fun Context.queryBoolean(name: String) = requestBooleanParam(this, query, name)

/**
 * 返回 `query` 参数.
 *
 * @param name 参数名称
 */
fun Context.queryInt(name: String) = requestIntParam(this, query, name)

/**
 * 返回 `query` 参数.
 *
 * @param name 参数名称
 */
fun Context.queryLong(name: String) = requestLongParam(this, query, name)

/**
 * 返回 `query` 参数.
 *
 * @param name 参数名称
 */
fun Context.queryDouble(name: String) = requestDoubleParam(this, query, name)

/**
 * 返回 `query` 参数.
 *
 * @param name 参数名称
 */
fun Context.queryString(name: String) = requestStringParam(this, query, name)

/**
 * 返回 `query` 参数并去除前后空格.
 *
 * @param name 参数名称
 */
fun Context.queryTrimString(name: String) = requestTrimStringParam(this, query, name)

/**
 * 返回 `form` 参数.
 *
 * `boolean` 取值设定.
 *
 * - true/false
 * - not 0/0
 * - on/off
 *
 * @param name 参数名称
 */
fun Context.formBoolean(name: String) = requestBooleanParam(this, form, name)

/**
 * 返回 `form` 参数.
 *
 * @param name 参数名称
 */
fun Context.formInt(name: String) = requestIntParam(this, form, name)

/**
 * 返回 `form` 参数.
 *
 * @param name 参数名称
 */
fun Context.formLong(name: String) = requestLongParam(this, form, name)

/**
 * 返回 `form` 参数.
 *
 * @param name 参数名称
 */
fun Context.formDouble(name: String) = requestDoubleParam(this, form, name)

/**
 * 返回 `form` 参数.
 *
 * @param name 参数名称
 */
fun Context.formString(name: String) = requestStringParam(this, form, name)

/**
 * 返回 `form` 参数并去除前后空格.
 *
 * @param name 参数名称
 */
fun Context.formTrimString(name: String) = requestTrimStringParam(this, form, name)


// =================================================================================================================
private fun requestBooleanParam(ctx: Context, where: String, name: String): Boolean {
    val v = requestTrimStringParam(ctx, where, name).toLowerCase()
    return v == "true" || v != "0" || v == "on"
}

private fun requestIntParam(ctx: Context, where: String, name: String): Int {
    val v = requestTrimStringParam(ctx, where, name)
    try {
        return v.toInt()
    } catch (e: NumberFormatException) {
        throw RequestParameterFormatException(where, name, """无法将 "$v" 转换为 int 类型""")
    }
}

private fun requestLongParam(ctx: Context, where: String, name: String): Long {
    val v = requestTrimStringParam(ctx, where, name)
    try {
        return v.toLong()
    } catch (e: NumberFormatException) {
        throw RequestParameterFormatException(where, name, """无法将 "$v" 转换为 long 类型""")
    }
}

private fun requestDoubleParam(ctx: Context, where: String, name: String): Double {
    val v = requestTrimStringParam(ctx, where, name)
    try {
        return v.toDouble()
    } catch (e: NumberFormatException) {
        throw RequestParameterFormatException(where, name, """无法将 "$v" 转换为 double 类型""")
    }
}

private fun requestStringParam(ctx: Context, where: String, name: String): String {
    val v = requestParam(ctx, where, name)
    if (v == null || v.isEmpty()) {
        throw MissingRequestParameterException(where, name)
    }
    return v
}

private fun requestTrimStringParam(ctx: Context, where: String, name: String): String {
    val v = requestParam(ctx, where, name)?.trim()
    if (v == null || v.isEmpty()) {
        throw MissingRequestParameterException(where, name)
    }
    return v
}

private fun requestParam(ctx: Context, where: String, name: String): String? = if (path == where) ctx.param(name) else if(query == where) ctx.queryParam(name) else ctx.formParam(name)
