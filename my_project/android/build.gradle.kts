allprojects {
    repositories {
        google()
        mavenCentral()
        flatDir {
            dirs("libs")
        }
    }
}

val newBuildDir: Directory = rootProject.layout.buildDirectory.dir("../../build").get()
rootProject.layout.buildDirectory.value(newBuildDir)

subprojects {
    val newSubprojectBuildDir: Directory = newBuildDir.dir(project.name)
    project.layout.buildDirectory.value(newSubprojectBuildDir)
}
subprojects {
    project.evaluationDependsOn(":app")
}

//tasks.register<Delete>("clean") {
//    delete(rootProject.layout.buildDirectory)
//}

tasks.named<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

// 👇 ADD THIS SECTION TO FIX THE ERROR
plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.tungsten.mobilesdkplugin"
    compileSdk = 33

    defaultConfig {
        minSdk = 26
        targetSdk = 33
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}