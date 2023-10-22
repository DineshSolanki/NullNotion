plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.12.0"
}

group = "com.abstractprogrammer"
version = "1.2"

repositories {
    mavenCentral()
}
dependencies {
    implementation("com.microsoft.sqlserver:mssql-jdbc:12.2.0.jre11")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("org.postgresql:postgresql:42.5.4")
    implementation("com.oracle.database.jdbc:ojdbc10:19.18.0.0")
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
