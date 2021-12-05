plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    compileSdk  = 31

    defaultConfig {
        applicationId = "dmitry.molchanov.compose"
        minSdk  = 21
        targetSdk  = 31
        versionCode  = 1
        versionName = "1.0"

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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Deps.compose_version
    }
}

dependencies {

    listOf(
        project(":common"),
        Deps.material,
        Deps.androidx_appcompat,
        Deps.androidx_activity_ktx,
        Deps.androidx_lifecycle_runtime_ktx,
        Deps.compose_activity,
        Deps.compose_material,
        Deps.compose_foundation,
        Deps.compose_icons_core,
        Deps.compose_icons_extended,
        Deps.compose_ui,
        Deps.compose_ui_tooling,
        Deps.compose_view_model,
    ).forEach (::implementation)

}