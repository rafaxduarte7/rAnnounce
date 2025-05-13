plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.3.0"
}

group = "com.rafaxplugins"
version = "0.1-ALPHA"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.elmakers.com/repository/")
    maven("https://repo.codemc.io/repository/maven-public/")
}

dependencies {
    compileOnly(fileTree("libs") { include("*.jar") })

    compileOnly("org.spigotmc:spigot:1.8.8-R0.1-SNAPSHOT")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
}

bukkit {
    name = "rAnnounce"
    main = "com.rafaxplugins.announce.AnnouncePlugin"
    author = "Rafax"
    version = "0.1-ALPHA"
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    shadowJar {
        archiveFileName.set("rAnnounce.jar")
        destinationDirectory = file("C:\\Users\\rafax.duarte7\\Documents\\Portfólio\\servidor teste\\plugins")
    }
}
