plugins {
    java
    id("com.diffplug.spotless") version "8.4.0"
}

group = "com.ofdun"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("ch.qos.logback:logback-classic:1.5.20")
    implementation("net.logstash.logback:logstash-logback-encoder:8.1")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.21.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.21.1")

    compileOnly("org.projectlombok:lombok:1.18.44")
    annotationProcessor("org.projectlombok:lombok:1.18.44")

    testCompileOnly("org.projectlombok:lombok:1.18.44")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.44")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

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
