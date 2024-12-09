plugins {
    kotlin("jvm") version "1.9.22"
}

dependencies {
    implementation("io.gitlab.arturbosch.detekt:detekt-api:1.22.0")
    testImplementation("io.gitlab.arturbosch.detekt:detekt-test:1.22.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.22")
}