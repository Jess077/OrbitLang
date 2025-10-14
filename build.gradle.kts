plugins {
    id("java")
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.gabby"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.7")
    implementation("it.unimi.dsi:fastutil:8.2.1")
    implementation("org.luaj:luaj-jse:3.0.1")
}

application {
    mainClass.set("com.softlocked.orbit.opm.project.OrbitREPL")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.softlocked.orbit.opm.project.OrbitREPL"
    }
}
tasks.shadowJar {
    manifest {
        attributes["Main-Class"] = "com.softlocked.orbit.opm.project.OrbitREPL"
    }
}

tasks.test {
    useJUnitPlatform()
}