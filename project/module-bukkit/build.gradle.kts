taboolib { subproject = true }

dependencies {
    api(project(":project:common"))
    api(project(":project:common-impl"))
    compileOnly("ink.ptms:nms-all:1.0.0")
    compileOnly("ink.ptms.core:v12004:12004:mapped")
    compileOnly("ink.ptms.core:v12004:12004:universal")
}