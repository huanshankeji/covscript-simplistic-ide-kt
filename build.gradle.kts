plugins {
    kotlin("jvm") version "1.4.10"
    application
    id("org.openjfx.javafxplugin") version "0.0.9"
}

group = "shreckye.covscript"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("no.tornado:tornadofx:1.7.20")
    implementation("org.apache.commons:commons-text:1.9")
    implementation("org.fxmisc.richtext:richtextfx:0.10.5")
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


// Generate app metadata for the project in Kotlin
val packageRelativePath = "shreckye/covscript/simplisticide"
val genKotlinDirPath = "$buildDir/gen/kotlin"
val generateAppMetadataForSourceString = "generateAppMetadataForSource"
val generatedAppMetadataFilename = "GeneratedAppMetadata.kt"
tasks.register(generateAppMetadataForSourceString) {
    inputs.property("version", version)
    outputs.dir(genKotlinDirPath)
    doFirst {
        val file = File("$genKotlinDirPath/$packageRelativePath/$generatedAppMetadataFilename")
        file.parentFile.mkdirs()
        file.writeText("const val VERSION = \"$version\"")
    }
}
sourceSets.main {
    withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) {
        kotlin.srcDir(genKotlinDirPath)
    }
}
tasks.compileKotlin {
    dependsOn(generateAppMetadataForSourceString)
}