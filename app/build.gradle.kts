import java.net.URI

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val defaultWebAppUrl = "https://scooljavanrood.ir/app/"
val webAppUrl = providers.gradleProperty("WEB_APP_URL")
    .orElse(providers.environmentVariable("WEB_APP_URL"))
    .orElse(defaultWebAppUrl)
    .get().trim()
val parsedUrl = try { URI(webAppUrl) } catch (e: Exception) { throw GradleException("WEB_APP_URL نامعتبر است: $webAppUrl") }
if (parsedUrl.scheme != "https" || parsedUrl.host.isNullOrBlank()) throw GradleException("WEB_APP_URL باید HTTPS باشد.")
val escaped = webAppUrl.trimEnd('/').replace("\\", "\\\\").replace("\"", "\\\"")

android {
    namespace = "ir.madreseyar.guardian"
    compileSdk = 35
    defaultConfig {
        applicationId = "ir.madreseyar.guardian"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0-parent-native"
        buildConfigField("String", "API_BASE_URL", "\"$escaped\"")
    }
    buildFeatures { buildConfig = true; compose = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    packaging { resources.excludes += setOf("META-INF/DEPENDENCIES", "META-INF/LICENSE*", "META-INF/NOTICE*") }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
