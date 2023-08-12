val project_version: String by project
val jdk_version: String by project
val hcmc_extension_version: String by project
val exposed_version: String by project

plugins {
    kotlin("jvm")
    id("maven-publish")
}

group = "studio.hcmc"
version = project_version

repositories {
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
}

kotlin {
    jvmToolchain(jdk_version.toInt())
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "studio.hcmc"
            artifactId = "exposed-transaction-extension"
            version = project_version
            from(components["java"])
        }
    }
}

dependencies {
    implementation("com.github.hcmc-studio:kotlin-coroutines-extension:$hcmc_extension_version")

    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
}