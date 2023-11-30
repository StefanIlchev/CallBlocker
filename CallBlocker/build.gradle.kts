import com.android.build.gradle.internal.api.ApkVariantOutputImpl
import java.util.Properties

plugins {
	id("com.android.application")
	kotlin("android")
}

val localProperties by extra(Properties().also {
	file("${rootDir.path}/local.properties").takeIf(File::isFile)?.bufferedReader()?.use(it::load)
})

kotlin {
	jvmToolchain(libs.versions.jvmToolchain.get().toInt())
}

android {
	buildToolsVersion = libs.versions.buildToolsVersion.get()
	compileSdk = libs.versions.compileSdk.get().toInt()
	namespace = "ilchev.stefan.callblocker"

	buildFeatures {
		buildConfig = true
	}

	defaultConfig {
		minSdk = libs.versions.minSdk.get().toInt()
		targetSdk = compileSdk
		versionCode = 17
		versionName = "$versionCode"
	}

	applicationVariants.configureEach {
		outputs.configureEach output@{
			if (this !is ApkVariantOutputImpl) return@output
			outputFileName = outputFile.run { "$nameWithoutExtension-$versionName.$extension" }
		}
	}

	signingConfigs {

		named("debug") {
			storeFile = file(localProperties.getProperty("store.file") ?: "${rootDir.path}/debug.keystore")
			storePassword = localProperties.getProperty("store.password") ?: "android"
			keyAlias = localProperties.getProperty("key.alias") ?: "androiddebugkey"
			keyPassword = localProperties.getProperty("key.password") ?: "android"
		}
	}

	buildTypes {

		named("release") {
			isMinifyEnabled = true
			isShrinkResources = true
			proguardFiles += getDefaultProguardFile("proguard-android-optimize.txt")
			signingConfig = signingConfigs["debug"]
		}
	}
}
