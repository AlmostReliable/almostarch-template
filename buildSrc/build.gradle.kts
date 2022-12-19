plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    gradleApi()
    implementation("com.google.code.gson:gson:2.10")
}

gradlePlugin {
    plugins {
        create("default") {
            id = "com.almostreliable.almostgradle"
            implementationClass = "com.almostreliable.mods.almostgradle.AlmostGradlePlugin"
        }
    }
}
