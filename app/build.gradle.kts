plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    // Dagger hilt plugins
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")

    // Serialization plugin used by supabase
    kotlin("plugin.serialization") version "2.2.10"
}

android {
    namespace = "com.example.doline"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.doline"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Env vars for supabase
        buildConfigField("String", "SUPABASE_URL", "\"https://mqistandrulavcbncpwn.supabase.co\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1xaXN0YW5kcnVsYXZjYm5jcHduIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDgzNzQ3NzIsImV4cCI6MjA2Mzk1MDc3Mn0.LWvreB-L64WLsdOY9o0tvr8ZOXXEthCEBWpaLIqSnsk\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true

    }
}

dependencies {
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui.util)
    val roomVersion = "2.8.4"
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.animation.core.lint)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui)
    implementation(libs.transport.runtime)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Dagger hilt deps
    implementation("com.google.dagger:hilt-android:2.59.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    "ksp"("com.google.dagger:hilt-android-compiler:2.59.2")


    // Navigation deps
    implementation("androidx.navigation:navigation-compose:2.9.7")
    implementation("androidx.compose.material:material-icons-core:1.7.7")

    // Coil image processer deps

    // Supabase deps
    implementation(platform("io.github.jan-tennert.supabase:bom:3.5.0"))
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:postgrest-kt")

    implementation("io.ktor:ktor-client-android:3.4.2")

    // Compose charts
    implementation ("io.github.ehsannarmani:compose-charts:0.2.5")

    // Room deps
    implementation("androidx.room:room-runtime:${roomVersion}")
    "ksp"("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:${roomVersion}")
    implementation("androidx.room:room-paging:${roomVersion}")

    // ViewModel + Lifecycle (recommended)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("androidx.datastore:datastore:1.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    // Gson
    implementation("com.google.code.gson:gson:2.14.0")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // OkHttp Logging (very useful for debugging)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Image processing
    implementation("io.coil-kt.coil3:coil-compose:3.4.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.4.0")

    // Material3 Adaptive
    implementation("androidx.compose.material3.adaptive:adaptive")
    implementation("androidx.compose.material3.adaptive:adaptive-layout")
}

