plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
}

android {
    namespace = "com.gb.restaurant"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.gb.restaurant"
        minSdk = 24
        targetSdk = 34
        versionCode = 21
        versionName = "4.8"

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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.4")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation(files("libs\\PosPrinterSDK.jar"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.google.firebase:firebase-messaging:23.2.1")
    implementation("com.squareup.retrofit2:retrofit:2.6.0")
    implementation("com.squareup.retrofit2:converter-gson:2.6.0")
    // dependency injection
    val daggerVer = 2.16 // or latest version

    implementation("com.google.dagger:dagger:$daggerVer")
    implementation("com.google.dagger:dagger-android-support:$daggerVer")
    kapt("com.google.dagger:dagger-android-processor:$daggerVer")
    kapt("com.google.dagger:dagger-compiler:$daggerVer")

    implementation("org.apache.commons:commons-lang3:3.6")

    implementation("androidx.multidex:multidex:2.0.1")


    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("com.afollestad.material-dialogs:core:3.1.0")
    implementation("com.afollestad.material-dialogs:datetime:3.1.0")
    implementation("com.afollestad.material-dialogs:input:3.1.0")
    implementation("androidx.fragment:fragment-ktx:1.6.1")
    implementation("com.google.android.play:core:1.10.3")
    implementation("org.glassfish:javax.annotation:10.0-b28")
}