package io.zhudy.duic

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
interface UserContext {

    companion object {
        const val CONTEXT_KEY = "user.context"
    }

    /**
     *
     */
    val email: String

}