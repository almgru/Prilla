@file:Suppress("DSL_SCOPE_VIOLATION")

import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)

    kotlin("kapt")
    alias(libs.plugins.hilt)
}

kotlin {
    version = libs.versions.kotlin.get()
    jvmToolchain(libs.versions.jvm.toolchain.get().toInt())
}

android {
    namespace = "com.almgru.prilla.android"

    compileSdk = libs.versions.sdk.compile.get().toInt()
    buildToolsVersion = libs.versions.sdk.buildTools.get()

    defaultConfig {
        applicationId = "com.almgru.prilla.android"
        minSdk = libs.versions.sdk.min.get().toInt()
        targetSdk = libs.versions.sdk.target.get().toInt()
        versionCode = 3
        versionName = "0.1.1"

        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val root = project.rootDir
            val keyStore = File(root, "prilla-apk-signing-key.jks")

            storeFile = keyStore
            storePassword = System.getenv("APK_SIGNING_KEY_PASSWORD")
            keyPassword = System.getenv("APK_SIGNING_KEY_PASSWORD")
            keyAlias = System.getenv("APK_SIGNING_KEY_ALIAS")
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.getByName("release")
        }
    }

    applicationVariants.all {
        outputs.all {
            (this as BaseVariantOutputImpl).outputFileName = "prilla-$versionName.apk"
        }
    }

    kotlinOptions {
        jvmTarget = libs.versions.jvm.target.get()
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.valueOf(libs.versions.jvm.sourceCompat.get())
        targetCompatibility = JavaVersion.valueOf(libs.versions.jvm.targetCompat.get())
    }

    viewBinding {
        enable = true
    }

    sourceSets {
        getByName("main") {
            // Needed to allow ktlint to find kotlin sources
            java.setSrcDirs(listOf("src/main/kotlin"))
        }
    }
}

dependencies {
    implementation(libs.kotlin.stdLib)
    implementation(libs.androidx.coreKtx)
    implementation(libs.androidx.activityKtx)
    implementation(libs.androidx.appCompat)
    implementation(libs.androidx.constraintLayout)
    implementation(libs.androidx.lifecycle.runtimeKtx)
    implementation(libs.androidx.lifecycle.viewmodelKtx)
    implementation(libs.androidx.dataStore)
    implementation(libs.protobuf.javaLite)
    implementation(libs.jsoup)
    implementation(libs.okHttp)
    implementation(libs.android.materialComponents)

    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    coreLibraryDesugaring(libs.tools.android.desugarJdkLibs)

    debugImplementation(libs.debug.okHttp.logInterceptor)

    testImplementation(libs.test.junit)
    androidTestImplementation(libs.test.androidx.junit)
    androidTestImplementation(libs.test.androidx.espressoCore)
}

kapt {
    correctErrorTypes = true
}

protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }

    // Generates the java Protobuf-lite code for the Protobufs in this project. See
    // https://github.com/google/protobuf-gradle-plugin#customizing-protobuf-compilation
    // for more information.
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}

detekt {
    toolVersion = libs.versions.plugin.detekt.get()

    tasks.withType<Detekt>().configureEach {
        reports {
            html.required.set(false)
            txt.required.set(false)
            md.required.set(false)
            sarif.required.set(false)
            xml.required.set(false)
        }
    }
}

ktlint {
    android.set(true)
}
