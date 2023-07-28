import com.android.build.gradle.internal.api.BaseVariantOutputImpl

/**
 * The first section in the build configuration applies the Android Gradle plugin
 * to this build and makes the android block available to specify
 * Android-specific build options.
 */
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

/**
 * Locate (and possibly download) a JDK used to build your kotlin
 * source code. This also acts as a default for sourceCompatibility,
 * targetCompatibility and jvmTarget. Note that this does not affect which JDK
 * is used to run the Gradle build itself, and does not need to take into
 * account the JDK version required by Gradle plugins (such as the
 * Android Gradle Plugin)
 */
kotlin {
    jvmToolchain(17)
}

/**
 * The android block is where you configure all your Android-specific
 * build options.
 */
android {
    namespace = "com.almgru.prilla.android"

    compileSdk = 34
    buildToolsVersion("34.0.0")

    defaultConfig {
        applicationId = "com.almgru.prilla.android"
        minSdk = 16
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        multiDexEnabled = true
    }

    signingConfigs {
        create("release") {
            val root = project.getRootDir()
            val keyStore = File(root, "prilla-apk-signing-key.jks")

            if (!keyStore.exists()) {
                throw GradleException("Missing keystore!")
            }

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

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.getByName("debug")
        }

        getByName("release") {
            isMinifyEnabled = true
            isDebuggable = false
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.getByName("release")
        }
    }

    applicationVariants.all{
        outputs.all {
            (this as BaseVariantOutputImpl).outputFileName = "prilla-$versionName.apk"
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.22")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.android.volley:volley:1.2.1")
    implementation("org.jsoup:jsoup:1.13.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.3")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.3")
    implementation("androidx.core:core-ktx:1.10.1")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")
}