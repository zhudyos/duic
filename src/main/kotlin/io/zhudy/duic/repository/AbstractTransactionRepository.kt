package io.zhudy.duic.repository

import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
abstract class AbstractTransactionRepository(
        private val transactionManager: PlatformTransactionManager
) {

    /**
     *
     */
    protected val transactionTemplate: TransactionTemplate
        get() = TransactionTemplate(transactionManager)

    /**
     *
     */
    protected val roTransactionTemplate: TransactionTemplate
        get() {
            val t = transactionTemplate
            t.isReadOnly = true
            return t
        }

}