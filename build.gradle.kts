plugins {
    kotlin("jvm") version "1.4.10"
    application
    id("org.openjfx.javafxplugin") version "0.0.9"
}

group = "shreckye.covscript"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("no.tornado:tornadofx:1.7.20")
}

javafx {
    version = "11.0.2"
    modules = listOf("javafx.controls", "javafx.graphics")
}

tasks.compileKotlin {
    kotlinOptions.jvmTarget = "11"
}

tasks.compileTestKotlin {
    kotlinOptions.jvmTarget = "11"
}

application.mainClassName = "shreckye.covscript.simplisticide.MainApp"