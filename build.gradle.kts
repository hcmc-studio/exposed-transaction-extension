plugins {
    kotlin("jvm") version "1.9.0"
}

group = "studio.hcmc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":kotlin-coroutines-extension"))

    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
}

kotlin {
    jvmToolchain(17)
}