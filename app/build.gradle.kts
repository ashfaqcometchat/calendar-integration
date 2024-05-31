plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.cometchat.calendarintegration"
    compileSdk = 34

    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
    }

    defaultConfig {
        applicationId = "com.cometchat.calendarintegration"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        manifestPlaceholders["file_provider"] = "com.cometchat.calendarintegration"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.cometchat:chat-uikit-android:4.3.9")

    implementation("com.google.api-client:google-api-client-android:1.33.0") {
        exclude(group = "org.apache.httpcomponents")
        exclude(group = "com.google.guava")
    }
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
    implementation("com.google.apis:google-api-services-calendar:v3-rev20220715-2.0.0") {
        exclude(group = "com.google.guava")
    }
    implementation("com.google.android.gms:play-services-auth:21.2.0")
}