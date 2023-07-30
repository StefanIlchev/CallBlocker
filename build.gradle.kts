import com.android.build.gradle.internal.api.ApkVariantOutputImpl
import java.util.Properties

plugins {
	id("com.android.application")
	kotlin("android")
}

repositories {
	google()
	mavenCentral()
	mavenLocal()
	maven { url = uri("https://jitpack.io") }
}

val localProperties = Properties()
file("local.properties").takeIf(File::isFile)?.bufferedReader()?.use(localProperties::load)

android {
	buildToolsVersion = "33.0.2"
	compileSdk = 33
	namespace = "ilchev.stefan.callblocker"

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	defaultConfig {
		minSdk = 28
		targetSdk = compileSdk
		versionCode = 12
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
			storeFile = file(localProperties.getProperty("storeFile") ?: "debug.keystore")
			storePassword = localProperties.getProperty("storePassword") ?: "android"
			keyAlias = localProperties.getProperty("keyAlias") ?: "androiddebugkey"
			keyPassword = localProperties.getProperty("keyPassword") ?: "android"
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

tasks.withType<JavaCompile>().configureEach {
	options.isDeprecation = true
}
