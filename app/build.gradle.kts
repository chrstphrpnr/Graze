plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.soulscrypt"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.soulscrypt"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //Responsive Size
    implementation ("com.intuit.sdp:sdp-android:1.1.0")


    implementation ("com.android.volley:volley:1.2.1")
    implementation ("com.google.android.gms:play-services-location:20.0.0")

    implementation ("de.hdodenhof:circleimageview:3.1.0")

    implementation ("com.squareup.picasso:picasso:2.8")

}