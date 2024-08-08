plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.16.0"
}

group = "com.abstractprogrammer"
version = "1.3"

repositories {
    mavenCentral()
}
dependencies {
    implementation("com.microsoft.sqlserver:mssql-jdbc:12.6.3.jre11")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("com.oracle.database.jdbc:ojdbc10:19.21.0.0")
    implementation("org.apache.commons:commons-text:1.12.0")
}
// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2024.2")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("com.intellij.java"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    patchPluginXml {
        sinceBuild.set("242")
    }

    signPlugin {
        certificateChainFile.set(file(System.getenv("CERTIFICATE_CHAIN")))
        privateKeyFile.set(file(System.getenv("PRIVATE_KEY")))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
