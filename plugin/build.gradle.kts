dependencies {
    taboo("ink.ptms:um:1.1.2")
}

taboolib {
    description {
        name(rootProject.name)
        contributors {
            name("坏黑")
        }
        dependencies {
            name("MythicMobs").optional(true)
            name("AttributePlus").optional(true)
        }
    }
    relocate("ink.ptms.um", "ink.ptms.zaphkiel.um")
}

tasks {
    jar {
        // 构件名
        archiveBaseName.set(rootProject.name)
        //archiveFileName.set("${rootProject.name}-${archiveFileName.get().substringAfter('-')}")
        // 打包子项目源代码
        rootProject.subprojects.forEach { from(it.sourceSets["main"].output) }
    }
    kotlinSourcesJar {
        // include subprojects
        rootProject.subprojects.forEach { from(it.sourceSets["main"].allSource) }
    }
}

publishing {
    publications {
        create<MavenPublication>("api") {
            artifactId = "api"
            groupId = "ink.ptms.zaphkiel"
            version = project.version.toString()
            artifact("${project.buildDir}/libs/${rootProject.name}-${rootProject.version}-api.jar")
            artifact(tasks["kotlinSourcesJar"]) {
                classifier = "sources"
            }
            println("> Apply \"$groupId:$artifactId:$version\"")
        }
    }
}