package com.scanner.buildscr

import org.gradle.api.JavaVersion

object AppConfig {

    const val ENABLE_R8_FULL_MODE: Boolean = true
    const val IS_RELEASE_MODE_DEBUGGABLE: Boolean = false

    object Version {
        const val MIN_SDK = 26
        const val TARGET_SDK = 34
        const val COMPILE_SDK = 35
        val JVM = JavaVersion.VERSION_21
        const val BUILD_TOOLS = "35.0.0"
        const val NDK = "27.0.11902837"
    }

    object Artifact {
        const val ID = "document_scanner_lib_android"
        const val GROUP_ID = "com.github.al-taie"
        const val VERSION = "1.0.11"
    }

    private const val GROUP_ID = "com.scanner"
    private const val APPLICATION_ID_SUFFIX = "app"
    private const val LIBRARY_ID_SUFFIX = "library"

    const val APPLICATION_ID = "${GROUP_ID}.${APPLICATION_ID_SUFFIX}"
    const val LIBRARY_ID = "${GROUP_ID}.${LIBRARY_ID_SUFFIX}"
    const val ANDROID_TEST_INSTRUMENTATION = "androidx.test.runner.AndroidJUnitRunner"
}
