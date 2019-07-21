package io.zhudy.duic

import org.springframework.test.context.TestContext
import org.springframework.test.context.TestExecutionListener

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
class StartupTestExecutionListener : TestExecutionListener {

    override fun beforeTestClass(testContext: TestContext) {
        System.setProperty("spring.profiles.active", "integration-test")
    }

}