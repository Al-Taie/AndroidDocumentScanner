import com.scanner.buildscr.AppConfig
import com.scanner.buildscr.AppConfig.Version

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = AppConfig.LIBRARY_ID
    compileSdk = Version.COMPILE_SDK
    ndkVersion = Version.NDK
    buildToolsVersion = Version.BUILD_TOOLS

    defaultConfig {
        minSdk = Version.MIN_SDK

        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }
        externalNativeBuild {
            ndkBuild {
                ndkPath = "src/main/jni"
                arguments += listOf("APP_PLATFORM=android-21", "-DANDROID_STL=c++_shared")
            }
        }
    }

    sourceSets {
        named("main") {
            jniLibs { srcDir("src/main/libs") }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = Version.JVM
        targetCompatibility = Version.JVM
    }

    kotlinOptions {
        jvmTarget = Version.JVM.toString()
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    jvmToolchain(Version.JVM.majorVersion.toInt())
}


dependencies {
    api(fileTree(baseDir = "libs") { include("*.jar") })
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    api(libs.subsampling.scale.image.view)
    api(libs.opencv)
    implementation(libs.androidx.exifinterface)
}
