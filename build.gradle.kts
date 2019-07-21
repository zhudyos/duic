import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    jacoco
    kotlin("jvm") version "1.3.41"
    kotlin("kapt") version "1.3.41"
    kotlin("plugin.spring") version "1.3.41"

    id("io.spring.dependency-management") version "1.0.8.RELEASE"
    id("org.springframework.boot") version "2.1.5.RELEASE"
    id("com.moowork.node") version "1.3.1"
    id("com.github.hierynomus.license") version "0.15.0"
    id("com.bmuschko.docker-remote-api") version "4.10.0"

    id("net.researchgate.release") version "2.8.0"
}

group = "io.zhudy.duic"

configurations {
    all {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
        exclude(group = "ch.qos.logback")
        exclude(module = "hibernate-validator")
        exclude(module = "nio-multipart-parser")
    }
}

dependencyManagement {
    imports {
        mavenBom("io.projectreactor:reactor-bom:Californium-SR9")
        mavenBom("org.springframework.boot:spring-boot-dependencies:2.1.5.RELEASE")
        mavenBom("org.springframework:spring-framework-bom:5.1.8.RELEASE")
        mavenBom("com.fasterxml.jackson:jackson-bom:2.9.9")
        mavenBom("org.junit:junit-bom:5.5.0")
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.2.1")

    val resilience4jVersion = "0.16.0"
    implementation("io.github.resilience4j:resilience4j-ratelimiter:$resilience4jVersion")
    implementation("io.github.resilience4j:resilience4j-reactor:$resilience4jVersion")

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("org.springframework:spring-jdbc")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.mongodb:mongodb-driver-reactivestreams:1.11.0")

    implementation("com.github.ben-manes.caffeine:caffeine:2.7.0")
    implementation("com.auth0:java-jwt:3.3.0")
    implementation("org.simplejavamail:simple-java-mail:5.1.6")
    implementation("io.github.java-diff-utils:java-diff-utils:4.0")
    implementation("org.springframework.security:spring-security-crypto:5.0.4.RELEASE")

    implementation("org.liquibase:liquibase-core:3.6.3")
    implementation("com.zaxxer:HikariCP:3.3.0")
    implementation("org.postgresql:postgresql:42.2.2")
    implementation("mysql:mysql-connector-java:8.0.14") {
        exclude(module = "protobuf-java")
    }

    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("com.squareup.okhttp3:mockwebserver:3.14.2")
    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("com.ninja-squad:springmockk:1.1.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    kapt("org.springframework.boot:spring-boot-configuration-processor")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    test {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
            exceptionFormat = TestExceptionFormat.FULL
        }
        failFast = true
    }

    jacoco {
        toolVersion = "0.8.4"
    }

    jacocoTestReport {
        reports {
            xml.isEnabled = true
            html.isEnabled = true
            html.destination = file("$buildDir/jacocoHtml")
        }
    }

    bootJar {
        manifest {
            attributes(mapOf(
                    "Implementation-Title" to "duic",
                    "Implementation-Version" to project.version
            ))
        }
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}