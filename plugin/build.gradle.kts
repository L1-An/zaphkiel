dependencies {
    taboo("ink.ptms:um:1.0.8")
}

taboolib {
    description {
        name(rootProject.name)
        contributors {
            name("坏黑")
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
}