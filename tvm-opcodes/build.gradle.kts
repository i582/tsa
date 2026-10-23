plugins {
    id("tsa.kotlin-conventions")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
    id("org.jmailen.kotlinter") version Versions.kotlinterPluginVersion
}

dependencies {
    implementation(project(":tvm-disasm"))
    implementation("org.ton:ton-kotlin-tvm:${Versions.tonKotlin}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinx_serialization}")
}

tasks.named("compileKotlin") {
    dependsOn(":tvm-disasm:generateInstructions")
}
