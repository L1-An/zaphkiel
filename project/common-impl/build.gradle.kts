taboolib { subproject = true }

dependencies {
    compileOnly(project(":project:common"))
    compileOnly("ink.ptms:nms-all:1.0.0")
    compileOnly("ink.ptms.core:v12004:12004:mapped")
    compileOnly("ink.ptms.core:v12004:12004:universal")
    compileOnly("ink.ptms:Sandalphon:1.4.1")
    compileOnly("public:AttributePlus:3.2.6")
    compileOnly("public:HeadDatabase:1.3.0")
    compileOnly("public:Tiphareth:1.0.0")
    compileOnly("ink.ptms:um:1.1.2")
    compileOnly("com.mojang:authlib:1.5.25")
    compileOnly(fileTree("libs"))
}