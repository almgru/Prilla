plugins {
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.kotlin).apply(false)
    alias(libs.plugins.hilt).apply(false)
    alias(libs.plugins.protobuf).apply(false)
    alias(libs.plugins.detekt).apply(false)
    alias(libs.plugins.ktlint).apply(false)
}

tasks.wrapper {
    gradleVersion = libs.versions.gradle.wrapper.get()
    distributionSha256Sum = libs.versions.gradle.sha256Sum.get()
}
