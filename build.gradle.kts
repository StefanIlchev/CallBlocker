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
file("${rootDir.path}/local.properties").takeIf(File::isFile)?.bufferedReader()?.use(localProperties::load)

android {
	buildToolsVersion = libs.versions.buildToolsVersion.get()
	compileSdk = libs.versions.compileSdk.get().toInt()
	namespace = "ilchev.stefan.callblocker"

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	defaultConfig {
		minSdk = libs.versions.minSdk.get().toInt()
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

tasks.withType<JavaCompile>().configureEach {
	options.isDeprecation = true
}
