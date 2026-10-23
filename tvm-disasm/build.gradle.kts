plugins {
    id("tsa.kotlin-conventions")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
    id("org.jmailen.kotlinter") version Versions.kotlinterPluginVersion
}

dependencies {
    implementation("org.ton:ton-kotlin-tvm:${Versions.tonKotlin}")
    implementation("org.ton:ton-kotlin-hashmap-tlb:${Versions.tonKotlin}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinx_serialization}")
}

tasks.register<JavaExec>("generateInstructions") {
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "GenerateInstKt"
    workingDir = rootProject.projectDir
}
