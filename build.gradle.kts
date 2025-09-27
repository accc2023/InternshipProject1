plugins {
    id("org.jetbrains.intellij") version "1.17.4"
    kotlin("jvm") version "2.0.0"
}

group = "com.arhan"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.9.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation(platform("org.junit:junit-bom:5.10.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

intellij {
    version.set("2024.1.4")
    type.set("IU")
    plugins.set(emptyList())
}

tasks {
    patchPluginXml {
        sinceBuild.set("241")
        untilBuild.set("")
    }
    runIde {
        jvmArgs = listOf("-Dkotlinx.coroutines.debug=on")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain(17)
}