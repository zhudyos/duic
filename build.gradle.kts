buildscript {
    dependencies {
        classpath("org.postgresql:postgresql:42.2.11")
        classpath("mysql:mysql-connector-java:8.0.19")
    }
}

plugins {
    jacoco
    kotlin("jvm") version "1.3.70"
    kotlin("kapt") version "1.3.70"
    kotlin("plugin.spring") version "1.3.70"

//    id("io.spring.dependency-management") version "1.0.8.RELEASE"
    id("org.springframework.boot") version "2.2.0.RELEASE"
    id("com.moowork.node") version "1.3.1"
    id("com.github.hierynomus.license") version "0.15.0"
    id("com.bmuschko.docker-remote-api") version "4.10.0"
    id("org.flywaydb.flyway") version "6.3.2"

    id("net.researchgate.release") version "2.8.0"
}

group = "io.zhudy.duic"

configurations {
    all {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
        exclude(group = "ch.qos.logback")
        exclude(module = "hibernate-validator")
        exclude(module = "nio-multipart-parser")
        exclude(module = "mockito-core")
    }
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:2.3.0.M3"))
    implementation(platform("io.r2dbc:r2dbc-bom:Arabba-SR2"))
    implementation(platform("com.fasterxml.jackson:jackson-bom:2.10.3"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.2.1")

    val kittyVersion = "2.0.0"
    implementation("io.zhudy.kitty:kitty-core:${kittyVersion}")
    implementation("io.zhudy.kitty:kitty-rest-problem:${kittyVersion}")
    implementation("io.zhudy.kitty.extensions:kitty-spring-webflux:${kittyVersion}")

    val resilience4jVersion = "0.16.0"
    implementation("io.github.resilience4j:resilience4j-ratelimiter:$resilience4jVersion")
    implementation("io.github.resilience4j:resilience4j-reactor:$resilience4jVersion")

    implementation("org.yaml:snakeyaml:1.25")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.valiktor:valiktor-core:0.8.0")
    implementation("com.github.ben-manes.caffeine:caffeine:2.7.0")
    implementation("io.jsonwebtoken:jjwt-impl:0.10.7")
    implementation("io.jsonwebtoken:jjwt-jackson:0.10.7")
    implementation("org.simplejavamail:simple-java-mail:5.1.6")
    implementation("org.bitbucket.cowwoc:diff-match-patch:1.2")
    implementation("com.googlecode.java-diff-utils:diffutils:1.3.0")

    implementation("com.github.seancfoley:ipaddress:5.2.1")
    implementation("org.apache.lucene:lucene-memory:8.5.0")

    implementation("org.springframework.security:spring-security-crypto:5.0.4.RELEASE")

    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.springframework.data:spring-data-r2dbc:1.0.0.RELEASE")
    implementation("io.r2dbc:r2dbc-postgresql")
    implementation("dev.miku:r2dbc-mysql")

    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("com.squareup.okhttp3:mockwebserver:3.14.2")
    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("com.ninja-squad:springmockk:1.1.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot.experimental:spring-boot-starter-data-r2dbc:0.1.0.M3")
    testImplementation("org.testcontainers:junit-jupiter:1.12.0")

//    kapt("org.springframework.boot:spring-boot-configuration-processor")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    test {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
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

    task<org.flywaydb.gradle.task.FlywayMigrateTask>("migratePostgreSQL") {
        setGroup("flyway")
        url = project.findProperty("xim.flyway.postgres.url")?.toString() ?: "jdbc:postgresql://192.168.31.62:5432/duic"
        user = project.findProperty("xim.flyway.postgres.user")?.toString() ?: "postgres"
        password = project.findProperty("xim.flyway.postgres.password")?.toString() ?: "123456"
        locations = arrayOf("filesystem:${project.projectDir.path}/db/PostgreSQL/migration")
    }

    task<org.flywaydb.gradle.task.FlywayMigrateTask>("migrateMySQL") {
        setGroup("flyway")
        url = project.findProperty("xim.flyway.mysql.url")?.toString() ?: "jdbc:mysql://192.168.31.61:3306"
        user = project.findProperty("xim.flyway.mysql.user")?.toString() ?: "root"
        password = project.findProperty("xim.flyway.mysql.password")?.toString() ?: "123456"
        schemas = arrayOf("duic")
        locations = arrayOf("filesystem:${project.projectDir.path}/db/MySQL/migration")
    }

    license {
        headerURI = com.hierynomus.gradle.license.LicenseBasePlugin::class.java.classLoader.getResource("headers/Apache-2.0")!!.toURI()
        mapping(mapOf(
                "vue" to "XML_STYLE"
        ))
        excludes(listOf(
                "db/migration/**"
        ))
        ext {
            set("year", "2017-2019")
            set("author", "the original author or authors")
        }
    }

    withType<com.hierynomus.gradle.license.tasks.LicenseCheck> {
        enabled = false
    }

    task<com.hierynomus.gradle.license.tasks.LicenseFormat>("licenseFormatWeb") {
        group = "license"
        source = fileTree(projectDir) {
            include(
                    "buildSrc/*.js",
                    "src/main/web/**/*.vue",
                    "src/main/web/**/*.html",
                    "src/main/web/**/*.js"
            )
        }
    }

    // docker 构建
    task<Copy>("copyDockerJar") {
        group = "docker"
        from(jar.get().archiveFile)
        into("$buildDir/docker")
        rename {
            it.replace("-$version", "")
        }
    }

    task<com.bmuschko.gradle.docker.tasks.image.Dockerfile>("createDockerfile") {
        dependsOn.add("copyDockerJar")

        group = "docker"
        destFile.set(file("$buildDir/docker/Dockerfile"))

        val jarName = "${project.name}.jar"
        from("openjdk:8u171-jre-slim-stretch")
        label(mapOf(
                "maintainer" to "Kevin Zou (kevinz@weghst.com)"
        ))
        copyFile(jarName, "/app/$jarName")
        workingDir("/app")
        volume("/app/logs")
        environmentVariable(mapOf(
                "TZ" to "Asia/Shanghai",
                "JVM_OPTS" to "-Xms1g -Xmx1g -XX:MetaspaceSize=128m",
                "JAVA_OPTS" to "-server -XX:+UseG1GC ${'$'}JVM_OPTS -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=logs/ -XX:+PrintGCDateStamps -verbose:gc -XX:+PrintGCDetails -Xloggc:logs/gc.log",
                "DUIC_OPTS" to "-Dspring.profiles.active=mongodb"
        ))
        exposePort(7777)
        defaultCommand("sh", "-c", "java ${'$'}JAVA_OPTS ${'$'}DUIC_OPTS -jar $jarName")
    }

    task<com.bmuschko.gradle.docker.tasks.image.DockerBuildImage>("buildImage") {
        dependsOn.add("createDockerfile")

        group = "docker"
        inputDir.set(file("$buildDir/docker"))
        tags.addAll("zhudyos/${project.name}:$version", "zhudyos/${project.name}:latest")
    }

    task<Copy>("copyRelease") {
        from(jar.get().archiveFile)
        into(file("$buildDir/releases"))
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.spring.io/milestone")
    maven("https://jitpack.io")
}