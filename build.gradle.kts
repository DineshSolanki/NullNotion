plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.12.0"
}

group = "com.abstractprogrammer"
version = "1.1"

repositories {
    mavenCentral()
}
dependencies {
    implementation("com.microsoft.sqlserver:mssql-jdbc:8.4.1.jre8")
}
// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.1.4")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("com.intellij.java"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    patchPluginXml {
        sinceBuild.set("221")
        untilBuild.set("231.*")
    }

    signPlugin {
        certificateChain.set(File(System.getenv("CERTIFICATE_CHAIN")).readText(Charsets.UTF_8))
        privateKey.set(File(System.getenv("PRIVATE_KEY")).readText(Charsets.UTF_8))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
