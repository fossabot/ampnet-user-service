import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("plugin.jpa") version "1.3.31"
    kotlin("jvm") version "1.3.31"
    kotlin("plugin.spring") version "1.3.31"
    id("org.springframework.boot") version "2.1.5.RELEASE"
    id("io.spring.dependency-management") version "1.0.7.RELEASE"
    id("com.google.cloud.tools.jib") version "1.2.0"
    id("org.jlleitschuh.gradle.ktlint") version "8.0.0"
    id("io.gitlab.arturbosch.detekt").version("1.0.0-RC14")
    jacoco
}

val jjwtVersion = "0.10.6"
val junitVersion = "5.3.2"

group = "com.ampnet"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-mail")

    implementation("org.springframework.social:spring-social-facebook:2.0.3.RELEASE")
    implementation("com.github.spring-social:spring-social-google:1.1.3")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.flywaydb:flyway-core")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    runtimeOnly("org.postgresql:postgresql")

    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    implementation("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")
    implementation("io.github.microutils:kotlin-logging:1.6.26")
    implementation("net.logstash.logback:logstash-logback-encoder:5.3")
    implementation("io.micrometer:micrometer-registry-prometheus:1.1.4")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("junit")
    }
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.subethamail:subethasmtp:3.1.7")

    // TODO: remove, only for java testing
    testImplementation("com.google.code.gson:gson:2.8.5")
    testImplementation("commons-io:commons-io:2.6")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

tasks.test {
    useJUnitPlatform()
}

jib {
    val dockerUsername: String = System.getenv("DOCKER_USERNAME") ?: "DOCKER_USERNAME"
    val dockerPassword: String = System.getenv("DOCKER_PASSWORD") ?: "DOCKER_PASSWORD"
    val identyumUsername: String = System.getenv("IDENTYUM_USERNAME") ?: "IDENTYUM_USERNAME"
    val identyumPassword: String = System.getenv("IDENTYUM_PASSWORD") ?: "IDENTYUM_PASSWORD"

    to {
        image = "ampnet/user-service:$version"
        auth {
            username = dockerUsername
            password = dockerPassword
        }
        tags = setOf("latest")
    }
    container {
        useCurrentTimestamp = true
        environment = mapOf("IDENTYUM_USERNAME" to identyumUsername, "IDENTYUM_PASSWORD" to identyumPassword)
    }
}

jacoco.toolVersion = "0.8.4"
tasks.jacocoTestReport {
    reports {
        xml.isEnabled = false
        csv.isEnabled = false
        html.destination = file("$buildDir/reports/jacoco/html")
    }
    sourceDirectories.setFrom(listOf(file("${project.projectDir}/src/main/kotlin")))
    classDirectories.setFrom(fileTree("$buildDir/classes/kotlin/main").apply {
        exclude("**/model/**", "**/pojo/**")
    })
    dependsOn(tasks.test)
}
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.7".toBigDecimal()
            }
        }
    }
    mustRunAfter(tasks.jacocoTestReport)
}

detekt {
    input = files("src/main/kotlin")
    filters = ".*/resources/.*,.*/build/.*"
}

task("qualityCheck") {
    dependsOn(tasks.ktlintCheck, tasks.jacocoTestReport, tasks.jacocoTestCoverageVerification, tasks.detekt)
}
