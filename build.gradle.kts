import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    `maven-publish`
    this.id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven {
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }

    maven {
        url = uri("https://jitpack.io")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    implementation("cloud.commandframework:cloud-core:1.8.3")
    implementation("cloud.commandframework:cloud-annotations:1.8.3")
    implementation("cloud.commandframework:cloud-paper:1.8.3")
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
}

val pluginVersion = property("plugin.version") as String
val pluginName = property("plugin.name") as String

group = "dev.niekkdev"
version = pluginVersion
description = "McBounties"
java.sourceCompatibility = JavaVersion.VERSION_17

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
    publications {
        register<MavenPublication>("gpr") {
            artifact(tasks.named<ShadowJar>("shadowJar").get())
        }
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}

tasks {
    this.shadowJar {
        this.mergeServiceFiles()
        this.archiveClassifier.set("")
        this.archiveVersion.set(version.toString())
        this.relocate("cloud.commandframework", "dev.niekkdev.shaded.cloud")
        this.relocate("io.leangen.geantyref", "dev.niekkdev.shaded.typetoken")
    }

    this.build {
        this.dependsOn(this@tasks.shadowJar)
    }

    this.processResources {
        filesMatching("plugin.yml") {
            expand(
                "name" to pluginName,
                "version" to version
            )
        }
    }
}
