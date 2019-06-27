package io.zhudy.duic.annotation

import org.springframework.context.annotation.Profile

/**
 * [Spring Annotation Programming Model](https://github.com/spring-projects/spring-framework/wiki/spring-annotation-programming-model)
 * 添加注解可在集群测试环境 `integration-test` 中禁用某些功能，作用范围与 [Profile] 一致。
 *
 * @see Profile
 * @author Kevin Zou (kevinz@weghst.com)
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention
@MustBeDocumented
@Profile("!integration-test")
annotation class NoIntegrationTest