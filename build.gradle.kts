val javaVersion = JavaVersion.VERSION_21
val ktorVersion = "2.3.12"
val kotestVersion = "5.9.1"
val jacksonVersion = "2.17.1"
val mockkVersion = "1.13.11"
val felleslibVersion = "0.0.140"

plugins {
    application
    kotlin("jvm") version "2.0.0"
    id("com.diffplug.spotless") version "6.25.0"
    // id("ca.cutterslade.analyze") version "1.9.1"
}

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
    maven {
        url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(kotlin("stdlib"))
    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("org.jetbrains:annotations:24.1.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.8.1")
//    implementation("io.prometheus:simpleclient:$prometheusVersion")
//    implementation("io.prometheus:simpleclient_common:$prometheusVersion")
    implementation("com.github.navikt.tiltakspenger-libs:fp-dtos:$felleslibVersion")
    implementation("com.github.navikt:rapids-and-rivers:2024061408021718344972.64ebbdb34321")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.7.3")
    implementation("org.jetbrains:annotations:24.1.0")
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

    testImplementation(platform("org.junit:junit-bom:5.10.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("io.ktor:ktor-client-mock-jvm:$ktorVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-json:$kotestVersion")
    testImplementation("io.kotest.extensions:kotest-assertions-arrow:1.4.0")
    testImplementation("io.kotest:kotest-extensions:$kotestVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("io.mockk:mockk-dsl-jvm:$mockkVersion")
    testImplementation("org.skyscreamer:jsonassert:1.5.3")
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
    /*
     analyzeClassesDependencies {
         warnUsedUndeclared = true
         warnUnusedDeclared = true
     }
     analyzeTestClassesDependencies {
         warnUsedUndeclared = true
         warnUnusedDeclared = true
     }
      */
}

task("addPreCommitGitHookOnBuild") {
    println("⚈ ⚈ ⚈ Running Add Pre Commit Git Hook Script on Build ⚈ ⚈ ⚈")
    exec {
        commandLine("cp", "./.scripts/pre-commit", "./.git/hooks")
    }
    println("✅ Added Pre Commit Git Hook Script.")
}
