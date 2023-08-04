plugins {
    /**
     * Use `apply false` in the top-level build.gradle file to add a Gradle
     * plugin as a build dependency but not apply it to the current (root)
     * project. Don't use `apply false` in sub-projects. For more information,
     * see Applying external plugins with same version to subprojects.
     */

    id("com.android.application").version("8.1.0").apply(false)
    id("com.android.library").version("8.1.0").apply(false)
    id("org.jetbrains.kotlin.android").version("1.9.0").apply(false)
    id("com.google.protobuf").version("0.9.4").apply(false)
    id("io.gitlab.arturbosch.detekt").version("1.23.1").apply(false)
    id("org.jlleitschuh.gradle.ktlint").version("11.3.1").apply(false)
}

