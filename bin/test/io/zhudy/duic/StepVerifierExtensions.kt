package io.zhudy.duic

import reactor.test.StepVerifier

/**
 * @author Kevin Zou (kevinz@weghst.com)
 */
fun StepVerifier.LastStep.expectError(bizCode: BizCode): StepVerifier = expectErrorMatches { it is BizCodeException && it.bizCode == bizCode }
