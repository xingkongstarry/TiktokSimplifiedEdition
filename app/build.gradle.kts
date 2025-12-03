plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.starry.tiktoksimplifiededition"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.starry.tiktoksimplifiededition"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    // --- 基础 AndroidX 依赖 (尝试使用 libs 引用，如果没有自动生成的 libs，请手动替换为字符串) ---
    // 如果报错 "unresolved reference: libs"，请将 libs.xxx 替换为注释中的字符串
    implementation(libs.appcompat)       // implementation("androidx.appcompat:appcompat:1.6.1")
    implementation(libs.material)        // implementation("com.google.android.material:material:1.11.0")
    implementation(libs.activity)        // implementation("androidx.activity:activity:1.8.0")
    implementation(libs.constraintlayout)// implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // --- 抖音作业特定依赖 (UI 组件) ---
    // 下拉刷新
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // --- 架构组件 (ViewModel / LiveData) ---
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")

    // --- 数据库 (Room) - 使用 Java ---
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("com.google.code.gson:gson:2.13.2")
    // 因为你是 Java 项目，使用 annotationProcessor 而不是 ksp/kapt
    annotationProcessor("androidx.room:room-compiler:$roomVersion")

    // --- 图片加载 (Glide) ---
    implementation("com.github.bumptech.glide:glide:4.15.1")
    // 如果需要生成 Glide App 模块，取消下面注释
    // annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    // --- 数据解析 (Gson) ---
    implementation("com.google.code.gson:gson:2.10.1")

    // --- 测试依赖 ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}