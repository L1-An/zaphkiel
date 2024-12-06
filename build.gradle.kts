@file:Suppress("PropertyName", "SpellCheckingInspection")

import io.izzel.taboolib.gradle.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    id("io.izzel.taboolib") version "2.0.22"
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
}

subprojects {
    apply<JavaPlugin>()
    apply(plugin = "io.izzel.taboolib")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    // TabooLib 配置
    taboolib {
        env {
            install(Basic, Bukkit, BukkitHook, BukkitUtil, XSeries, BukkitUI,
                BukkitNMSUtil, Kether, DatabasePlayer, I18n, CommandHelper,
                MinecraftChat)
        }
        version { taboolib = "6.2.0" }
    }

    // 全局仓库
    repositories {
        mavenLocal()
        mavenCentral()
    }
    // 全局依赖
    dependencies {
        compileOnly("org.apache.commons:commons-lang3:3.12.0")
        compileOnly("com.google.guava:guava:30.1.1-jre")
        compileOnly("com.google.code.gson:gson:2.8.8")
        compileOnly("ink.ptms.core:v12004:12004:mapped")
        compileOnly("ink.ptms.core:v12004:12004:universal")
        compileOnly(kotlin("stdlib"))
    }

    // 编译配置
    java {
        withSourcesJar()
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = listOf("-Xjvm-default=all", "-Xextended-compiler-checks")
        }
    }
}

gradle.buildFinished {
    buildDir.deleteRecursively()
}

kotlin {
    sourceSets.all {
        languageSettings {
            languageVersion = "2.0"
        }
    }
}