object Deps {

    const val androidx_activity_ktx = "androidx.activity:activity-ktx:1.4.0"
    const val androidx_appcompat = "androidx.appcompat:appcompat:1.3.1"
    //const val androidx_activity_ktx = "'androidx.activity:activity-ktx:1.4.0'"
    const val material = "com.google.android.material:material:1.4.0"

    // Exo player
    const val exo_player = "com.google.android.exoplayer:exoplayer:2.16.1"

    // Lifecycle
    const val androidx_lifecycle_runtime_ktx =
        "androidx.lifecycle:lifecycle-runtime-ktx:2.4.0"
    const val androidx_lifecycle_viewmodel_ktx =
        "androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0"

    /* Compose */
    const val compose_version = "1.0.5"

    const val compose_activity = "androidx.activity:activity-compose:1.3.1"
    const val compose_ui = "androidx.compose.ui:ui:$compose_version"

    // Tooling support (Previews, etc.)
    const val compose_ui_tooling = "androidx.compose.ui:ui-tooling:$compose_version"

    // Foundation (Border, Background, Box, Image, Scroll, shapes, animations, etc.)
    const val compose_foundation = "androidx.compose.foundation:foundation:$compose_version"

    // Material Design
    const val compose_material = "androidx.compose.material:material:$compose_version"

    // Material design icons
    const val compose_icons_core = "androidx.compose.material:material-icons-core:$compose_version"
    const val compose_icons_extended =
        "androidx.compose.material:material-icons-extended:$compose_version"

    // Integration with ViewModels
    const val compose_view_model = "androidx.lifecycle:lifecycle-viewmodel-compose:2.4.0"

    object Instrumental {
        const val androidx_junit_ext = "androidx.test.ext:junit:1.1.3"
        const val androidx_espresso_core = "androidx.test.espresso:espresso-core:3.4.0"
    }
}