val javaVersion = JavaVersion.VERSION_17
val ktorVersion = "2.3.0"
val kotestVersion = "5.5.5"
val jacksonVersion = "2.14.2"
val mockkVersion = "1.13.5"
val felleslibVersion = "0.0.31"

plugins {
    application
    kotlin("jvm") version "1.8.21"
    id("com.diffplug.spotless") version "6.17.0"
    id("ca.cutterslade.analyze") version "1.9.0"
}

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
    maven("https://jitpack.io")
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(kotlin("stdlib"))
    implementation("ch.qos.logback:logback-classic:1.4.7")
    implementation("net.logstash.logback:logstash-logback-encoder:7.3")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("org.jetbrains:annotations:24.0.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")
//    implementation("io.prometheus:simpleclient:$prometheusVersion")
//    implementation("io.prometheus:simpleclient_common:$prometheusVersion")
    implementation("com.github.navikt.tiltakspenger-libs:fp-dtos:$felleslibVersion")
    implementation("com.github.navikt:rapids-and-rivers:2023031511211678875716.e6e2c9250860")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.6.4")
    implementation("org.jetbrains:annotations:24.0.1")
    implementation("com.natpryce:konfig:1.6.10.0")

    implementation("io.ktor:ktor-client-auth:$ktorVersion")
    implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-cio-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jvm:$ktorVersion")
    implementation("io.ktor:ktor-utils-jvm:$ktorVersion")
    implementation("io.ktor:ktor-http-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-logging-jvm:$ktorVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    testImplementation(platform("org.junit:junit-bom:5.9.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("io.ktor:ktor-client-mock-jvm:$ktorVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-json:$kotestVersion")
    testImplementation("io.kotest.extensions:kotest-assertions-arrow:1.3.0")
    testImplementation("io.kotest:kotest-extensions:$kotestVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("io.mockk:mockk-dsl-jvm:$mockkVersion")
    testImplementation("org.skyscreamer:jsonassert:1.5.1")
}

configurations.all {
    // exclude JUnit 4
    exclude(group = "junit", module = "junit")
}

application {
    mainClass.set("no.nav.tiltakspenger.fp.ApplicationKt")
}

java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

spotless {
    kotlin {
        ktlint("0.48.2")
    }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = javaVersion.toString()
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = javaVersion.toString()
        kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }
    test {
        // JUnit 5 support
        useJUnitPlatform()
        // https://phauer.com/2018/best-practices-unit-testing-kotlin/
        systemProperty("junit.jupiter.testinstance.lifecycle.default", "per_class")
    }
    analyzeClassesDependencies {
        warnUsedUndeclared = true
        warnUnusedDeclared = true
    }
    analyzeTestClassesDependencies {
        warnUsedUndeclared = true
        warnUnusedDeclared = true
    }
}
