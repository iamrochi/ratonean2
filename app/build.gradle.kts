import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrainsKotlinSerialization)
    alias(libs.plugins.ksp)
}

// P0.1: leer secrets locales sin commitearlos (API URLs en local.properties)
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
// Crashlytics listo: se activa solo cuando pongas app/google-services.json.
// Sin ese archivo el build sigue verde y AppLogger no-opéa el reporte.
val hasGoogleServicesJson = file("google-services.json").exists()
if (hasGoogleServicesJson) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}
// BASE_URL por ambiente: local.properties > env > default. Release exige HTTPS.
val debugBaseUrl: String =
    localProps.getProperty("API_BASE_URL_DEBUG")
        ?: System.getenv("API_BASE_URL_DEBUG")
        ?: "http://10.0.2.2:8080"
val releaseBaseUrl: String =
    localProps.getProperty("API_BASE_URL_RELEASE")
        ?: System.getenv("API_BASE_URL_RELEASE")
        ?: "https://v2202609396147529037.quicksrv.de"

android {
    namespace = "com.example.ratonean2_app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.ratonean2_app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"$debugBaseUrl\"")
            buildConfigField("boolean", "ALLOW_CLEARTEXT", "true")
            // Crashlytics también en debug cuando hay json (para probar crashes no-fatales).
            buildConfigField("boolean", "CRASHLYTICS_ENABLED", if (hasGoogleServicesJson) "true" else "false")
        }
        release {
            // P1.5: R8 activado para prod. Reglas en proguard-rules.pro.
            // Si un release rompe por ofuscación, el primer sospechoso es un @Serializable
            // nuevo sin la keep de proguard.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_URL", "\"$releaseBaseUrl\"")
            buildConfigField("boolean", "ALLOW_CLEARTEXT", "false")
            buildConfigField("boolean", "CRASHLYTICS_ENABLED", if (hasGoogleServicesJson) "true" else "false")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/ASL2.0"
            excludes += "META-INF/*.kotlin_module"
            excludes += "META-INF/LICENSE-notice.md"
            excludes += "META-INF/*.txt"
            excludes += "META-INF/*.md"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.material:material-icons-extended:1.7.5")

    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil-gif:2.6.0")

    // MapLibre
    implementation(libs.maplibre.android)
    implementation(libs.maplibre.compose)
    implementation("org.maplibre.gl:android-plugin-annotation-v9:3.0.2")

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    // Offline-first: prune nocturno del caché (evita DELETE masivo en hot path).
    implementation(libs.androidx.work.runtime)

    // Using bundles to clean up redundant lines
    implementation(libs.bundles.ktor.client)
    implementation(libs.bundles.play.services)
    implementation(libs.bundles.compose)
    implementation(libs.bundles.compose.testing)
    implementation(libs.bundles.test.libraries)
    implementation(libs.androidx.foundation)
    implementation(libs.material3)
    implementation(libs.foundation)
    testImplementation(libs.bundles.junit5)

    // Core testing dependencies (these are in the `testing` bundle)

    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // Bundles for testing Ktor
    testImplementation(libs.bundles.ktor.testing)
    // P1.6: migración Room testeable + MockEngine ya incluido arriba.
    testImplementation("androidx.room:room-testing:2.7.0")
    androidTestImplementation("androidx.room:room-testing:2.7.0")

    // Other dependencies
    implementation("com.google.accompanist:accompanist-permissions:0.36.0")
    // Mapa: solo MapLibre (Google Maps eliminado en P2).
    implementation("io.insert-koin:koin-core:3.5.6")
    implementation("io.insert-koin:koin-android:3.5.6")
    implementation("io.insert-koin:koin-androidx-compose:3.5.6")
    implementation(libs.gids.signin)
    implementation(libs.gids.auth.api.phone)
    implementation(libs.navigation.compose)
    testImplementation(kotlin("test"))

    // Crashlytics: solo compila cuando hay google-services.json (ver arriba).
    if (hasGoogleServicesJson) {
        implementation(platform(libs.firebase.bom))
        implementation(libs.firebase.crashlytics)
        implementation(libs.firebase.analytics)
    }
}