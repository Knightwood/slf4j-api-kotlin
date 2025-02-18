@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    kotlin("jvm")
}



dependencies {
    implementation(libs.kotlin.coroutines.core)
    implementation("org.slf4j:slf4j-api:2.0.15")
    // logback-classic 1.3.15是最后的java 8 版本，后续版本要求java 11
    api("ch.qos.logback:logback-classic:1.5.12")
    implementation(project(":impl"))
}
