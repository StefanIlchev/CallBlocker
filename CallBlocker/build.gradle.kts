import com.android.build.api.variant.impl.VariantOutputImpl
import com.android.build.gradle.tasks.PackageApplication
import org.apache.tools.ant.types.Commandline
import java.util.Properties

plugins {
	id("com.android.application")
	kotlin("android")
	id("com.github.breadmoirai.github-release")
}

val localProperties: Properties by ext

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

	signingConfigs {

		named("debug") {
			storeFile = rootProject.file(localProperties.getProperty("store.file") ?: "debug.keystore")
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

androidComponents {

	onVariants { variant ->
		variant.outputs.forEach {
			if (it !is VariantOutputImpl) return@forEach
			it.outputFileName = file(it.outputFileName.get()).run {
				"$nameWithoutExtension-${it.versionName.get()}.$extension"
			}
		}
	}
}

dependencies {
	androidTestImplementation(libs.androidTest.runner)
	androidTestImplementation(libs.androidTest.junit)
	androidTestImplementation(libs.androidTest.uiautomator)
	testImplementation(libs.test.junit)
}

System.getProperty("adb.args")?.let {
	tasks.register<Exec>("adb") {
		group = project.name
		executable = android.adbExecutable.path
		args(*Commandline.translateCommandline(it))
	}
}

tasks.githubRelease {
	val packageRelease = tasks.named<PackageApplication>("packageRelease")
	dependsOn(packageRelease)
	owner = localProperties.getProperty("github.owner")
	repo = localProperties.getProperty("github.repo")
	authorization = localProperties.getProperty("github.authorization")
	tagName = "v${android.defaultConfig.versionName}"
	targetCommitish = localProperties.getProperty("github.targetCommitish")
	releaseName = android.defaultConfig.versionName
	body = localProperties.getProperty("github.body")
	prerelease = true
	releaseAssets.from(packageRelease.map { task ->
		task.outputDirectory.asFileTree.filter { it.extension == "apk" }.sorted()
	})
}
