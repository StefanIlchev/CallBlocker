import com.android.build.gradle.internal.api.ApkVariantOutputImpl
import org.apache.tools.ant.types.Commandline
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
	testNamespace = "$namespace.test"
	testBuildType = System.getProperty("test.build.type") ?: "debug"

	buildFeatures {
		buildConfig = true
	}

	defaultConfig {
		minSdk = libs.versions.minSdk.get().toInt()
		targetSdk = compileSdk
		versionCode = (System.getProperty("version.code") ?: libs.versions.versionCode.get()).toInt()
		versionName = System.getProperty("version.name") ?: "$versionCode"
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		buildConfigField(
			"String",
			"LATEST_RELEASE_URL",
			"\"${localProperties.getProperty("latest.release.url") ?: ""}\""
		)
		buildConfigField(
			"String",
			"PROJECT_NAME",
			"\"${project.name}\""
		)
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
			val isNotTestBuildType = testBuildType != name
			isMinifyEnabled = isNotTestBuildType
			isShrinkResources = isNotTestBuildType
			if (isNotTestBuildType) {
				proguardFiles += getDefaultProguardFile("proguard-android-optimize.txt")
			}
			signingConfig = signingConfigs["debug"]
		}
	}
}

dependencies {
	androidTestImplementation(libs.androidTest.runner)
	androidTestImplementation(libs.androidTest.junit)
	androidTestImplementation(libs.androidTest.uiautomator)
	testImplementation(libs.test.junit)
}

System.getProperty("adb.args")?.let { adbArgs ->
	tasks.register<Exec>("adb") {
		group = project.name
		executable = android.adbExecutable.path
		args(*Commandline.translateCommandline(adbArgs))
	}
}
