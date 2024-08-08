plugins {
    alias(libs.plugins.android.application)
    id("androidx.room") version "2.6.1" apply false
}

android {
    namespace = "com.example.youtubepart1"
    compileSdk = 34

    packagingOptions {
        resources {
            excludes += "META-INF/DEPENDENCIES"
        }
    }

    defaultConfig {
        applicationId = "com.example.youtubepart1"
        minSdk = 26
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    implementation("com.google.code.gson:gson:2.11.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("com.google.android.material:material:1.4.0")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("org.apache.httpcomponents.client5:httpclient5:5.3.1")
}
