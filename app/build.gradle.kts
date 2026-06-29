plugins {
    id("com.android.application")
}

android {
    namespace = "dev.fzer0x.fucksolidexplorer"
    compileSdk = 37

    defaultConfig {
        applicationId = "dev.fzer0x.fucksolidexplorer"
        minSdk = 29
        targetSdk = 37
        versionCode = 7
        versionName = "1.6"
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
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
    compileOnly("de.robv.android.xposed:api:82")
}
