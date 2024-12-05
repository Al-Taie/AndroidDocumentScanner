import com.scanner.buildscr.AppConfig
import com.scanner.buildscr.AppConfig.Version
import org.gradle.internal.impldep.org.apache.sshd.common.util.io.IoUtils.buildPath
import org.gradle.kotlin.dsl.implementation
import org.gradle.language.nativeplatform.internal.Dimensions.applicationVariants

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    `maven-publish`
}

android {
    ndkVersion = Version.NDK
    namespace = AppConfig.LIBRARY_ID
    compileSdk = Version.COMPILE_SDK
    buildToolsVersion = Version.BUILD_TOOLS

    defaultConfig {
        minSdk = Version.MIN_SDK

        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }
        externalNativeBuild {
            ndkBuild {
                arguments += listOf("APP_PLATFORM=android-24", "-DANDROID_STL=c++_shared")
            }
        }
    }

    sourceSets {
        named("main") {
            jniLibs {
                srcDirs("src/main/jniLibs")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            consumerProguardFiles("consumer-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = Version.JVM
        targetCompatibility = Version.JVM
    }

    kotlinOptions {
        jvmTarget = Version.JVM.toString()
    }

    buildFeatures { compose = true }
    packaging {
        jniLibs.keepDebugSymbols += "**/*.so"
    }

//    externalNativeBuild {
//        cmake {
//            path("CMakeLists.txt")
//        }
//    }
//    lint {
//        abortOnError = false
//    }

    libraryVariants.all {
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "${AppConfig.Artifact.ID}-${AppConfig.Artifact.VERSION}-${buildType.name}.aar"
        }
    }
}

kotlin {
    jvmToolchain(Version.JVM.majorVersion.toInt())
}


dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.window)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.implementation.core)
    implementation(libs.bundles.implementation.compose)
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Al-Taie/document_scanner_lib_android")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }

    publications {
        register<MavenPublication>("release") {
            afterEvaluate {
                groupId = AppConfig.Artifact.ID
                version = AppConfig.Artifact.VERSION
                artifactId = AppConfig.Artifact.GROUP_ID
                val buildPath = layout.buildDirectory.dir("/outputs/aar").toString()
                from(components["release"])
                artifact(tasks["sourcesJar"])
                artifact(tasks["javadocJar"])
                artifact("${buildPath}/$groupId-$version-release.aar")
            }
        }
    }
}

tasks.register<Jar>("sourcesJar") {
    from(android.sourceSets["main"].java.srcDirs)
    archiveClassifier.set("sources")
    archiveBaseName.set(AppConfig.Artifact.ID)
}

tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    archiveBaseName.set(AppConfig.Artifact.ID)
}
