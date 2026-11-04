import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
}

object TestAdUnits {
    const val APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val BANNER = "ca-app-pub-3940256099942544/9214589741"
    const val INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
    const val APP_OPEN = "ca-app-pub-3940256099942544/9257395921"
}

// IDs reales de AdMob en admob.properties (no se sube a git; ver admob.properties.example).
// Si falta un ID se usa el de prueba de Google, así nunca se publica un ID vacío.
val admob = Properties().apply {
    val file = rootProject.file("admob.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun adProperty(name: String, fallback: String = ""): String =
    (admob.getProperty(name) ?: findProperty(name) as String?)?.trim()?.takeIf { it.isNotEmpty() } ?: fallback

// Hashes de tus móviles y los de los testers, separados por comas: reciben anuncios de prueba
// aunque instalen la versión de Play.
val testDeviceIds: String = adProperty("admob.testDeviceIds")

android {
    namespace = "com.oeaon.hydrodaily"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.oeaon.hydrodaily"
        minSdk = 24
        targetSdk = 36
        versionCode = 11
        versionName = "2.1"
        buildConfigField("String", "TEST_DEVICE_IDS", "\"$testDeviceIds\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            manifestPlaceholders["admobAppId"] = TestAdUnits.APP_ID
            buildConfigField("String", "AD_BANNER_ID", "\"${TestAdUnits.BANNER}\"")
            buildConfigField("String", "AD_INTERSTITIAL_ID", "\"${TestAdUnits.INTERSTITIAL}\"")
            buildConfigField("String", "AD_APP_OPEN_ID", "\"${TestAdUnits.APP_OPEN}\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders["admobAppId"] = adProperty("admob.appId", TestAdUnits.APP_ID)
            buildConfigField("String", "AD_BANNER_ID", "\"${adProperty("admob.bannerId", TestAdUnits.BANNER)}\"")
            buildConfigField("String", "AD_INTERSTITIAL_ID", "\"${adProperty("admob.interstitialId", TestAdUnits.INTERSTITIAL)}\"")
            buildConfigField("String", "AD_APP_OPEN_ID", "\"${adProperty("admob.appOpenId", TestAdUnits.APP_OPEN)}\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics)
    implementation(libs.play.review)
    implementation(libs.play.billing)
    implementation(libs.play.services.ads)
    implementation(libs.user.messaging.platform)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
}
