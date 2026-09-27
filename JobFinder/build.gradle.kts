import java.util.Locale

plugins {
    java
    jacoco
    id("org.springframework.boot") version "4.0.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "8.4.0"
}

group = "com.ofdun"
version = "0.0.1-SNAPSHOT"
description = "JobFinder"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
    create("mockitoAgent") {
        isTransitive = false
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("net.logstash.logback:logstash-logback-encoder:8.1")
    implementation("io.qdrant:client:1.17.0")
    implementation("com.google.protobuf:protobuf-java:4.28.2")
    implementation("com.google.guava:guava:33.0.0-jre")
    implementation("org.hibernate.orm:hibernate-core:7.3.0.Final")
    implementation("org.hibernate.validator:hibernate-validator:9.1.0.Final")
    implementation("net.logstash.logback:logstash-logback-encoder:8.1")

    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.springframework.boot:spring-boot-starter-flyway")

    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    testImplementation("org.testcontainers:testcontainers-mongodb")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-mongodb-test:4.0.4")

    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation(platform("org.testcontainers:testcontainers-bom:2.0.4"))
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-data-redis-test:4.0.5")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("org.springframework.security:spring-security-test")
    add("mockitoAgent", "org.mockito:mockito-core")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
    jvmArgs("-javaagent:${configurations["mockitoAgent"].singleFile.absolutePath}")
}

jacoco {
    toolVersion = "0.8.14"
}

tasks.test {
    description = "Run *Test classes without Docker or external services."
    include("**/*Test.class")
    maxParallelForks = providers.gradleProperty("testForks").orElse("1").get().toInt()
    forkEvery = providers.gradleProperty("testForkEvery").orElse("0").get().toLong()
    systemProperty("junit.jupiter.execution.parallel.enabled", "false")
    val randomOrder = providers.gradleProperty("randomOrder").orElse("false").get().toBoolean()
    if (randomOrder) {
        val seed = providers.gradleProperty("testSeed").orElse(System.currentTimeMillis().toString()).get()
        systemProperty("junit.jupiter.testclass.order.default", "org.junit.jupiter.api.ClassOrderer\$Random")
        systemProperty("junit.jupiter.testmethod.order.default", "org.junit.jupiter.api.MethodOrderer\$Random")
        systemProperty("junit.jupiter.execution.order.random.seed", seed)
        doFirst { logger.lifecycle("JUnit random seed: $seed") }
    }
    testLogging {
        events("failed", "skipped")
        showStandardStreams = providers.gradleProperty("showTestProcesses").orElse("false").get().toBoolean()
    }
    reports {
        html.required.set(true)
        junitXml.required.set(true)
    }
    finalizedBy(tasks.jacocoTestReport)
}

tasks.register("unitTest") {
    group = "verification"
    description = "Alias for test: run unit and in-memory component tests without Docker."
    dependsOn(tasks.test)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        html.required.set(true)
        xml.required.set(true)
        csv.required.set(true)
    }
}

tasks.register("coverageSummary") {
    group = "verification"
    description = "Print JaCoCo line and branch counters for all production classes."
    dependsOn(tasks.jacocoTestReport)
    doLast {
        val rows = tasks.jacocoTestReport.get().reports.csv.outputLocation.get().asFile
            .readLines().drop(1).filter { it.isNotBlank() }.map { it.split(',') }
        for ((label, missedIndex, coveredIndex) in listOf(Triple("LINE", 7, 8), Triple("BRANCH", 5, 6))) {
            val missed = rows.sumOf { it[missedIndex].toLong() }
            val covered = rows.sumOf { it[coveredIndex].toLong() }
            val total = missed + covered
            val percentage = if (total == 0L) "n/a" else "%.2f%%".format(Locale.ROOT, covered * 100.0 / total)
            logger.lifecycle("$label: $covered/$total ($percentage), missed=$missed")
        }
    }
}

tasks.register<Test>("integrationTest") {
    description = "Run all *IT classes using Docker; requires the prebuilt Ollama image."
    group = "verification"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    include("**/*IT.class")
    shouldRunAfter(tasks.test)
}

tasks.register<Test>("testPostgres") {
    group = "verification"
    description = "Run PostgreSQL integration tests using Testcontainers (requires Docker)."
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    include("**/*IT.class")
    useJUnitPlatform {
        includeTags("postgres")
    }
    systemProperty("spring.profiles.active", "test-postgres")
}

tasks.register<Test>("testMongo") {
    group = "verification"
    description = "Run MongoDB integration tests using Testcontainers (requires Docker)."
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    include("**/*IT.class")
    useJUnitPlatform {
        includeTags("mongo")
    }
    systemProperty("spring.profiles.active", "test-mongo")
}

tasks.register("databaseIntegrationTest") {
    group = "verification"
    description = "Run PostgreSQL and MongoDB integration tests only (requires Docker)."
    dependsOn("testPostgres", "testMongo")
}

tasks.register("testAll") {
    group = "verification"
    description = "Run unit tests and every integration test (requires Docker and the Ollama image)."
    dependsOn("unitTest", "integrationTest")
}

apply(from = "../ci/testing.gradle")

spotless {
    ratchetFrom("origin/main")

    format("misc") {
        target("*.gradle", ".gitattributes", ".gitignore")

        trimTrailingWhitespace()
        leadingSpacesToTabs()
        endWithNewline()
    }

    java {
        googleJavaFormat("1.35.0")
            .aosp()
            .reflowLongStrings()
            .skipJavadocFormatting()
        importOrder()
        removeUnusedImports()

        trimTrailingWhitespace()
        endWithNewline()
    }
}
