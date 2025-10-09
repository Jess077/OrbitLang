plugins {
    id("java")
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

tasks.test {
    useJUnitPlatform()
}