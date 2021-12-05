plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    compileSdk =  31

    defaultConfig {
        applicationId  = "dmitry.molchanov.viewvscompose"
        minSdk  = 21
        targetSdk  = 31
        versionCode  = 1
        versionName  = "1.0"

        testInstrumentationRunner  = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility  = JavaVersion.VERSION_1_8
        targetCompatibility  = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    listOf(
        project(":common"),
        Deps.exo_player,
        Deps.material,
        Deps.androidx_activity_ktx,
        Deps.androidx_lifecycle_runtime_ktx
    ).forEach (::implementation)

}