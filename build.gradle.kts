plugins {
    java
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "AroundTheEurope"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("jakarta.validation:jakarta.validation-api:3.1.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // RabbitMQ
    implementation("org.springframework.amqp:spring-rabbit:3.1.6")
    // Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis:3.3.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
