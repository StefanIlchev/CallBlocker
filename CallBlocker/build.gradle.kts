import com.android.build.api.variant.impl.VariantOutputImpl
import com.android.build.gradle.tasks.PackageApplication
import org.apache.tools.ant.types.Commandline
import stef40.buildsrc.getPropertyValue

plugins {
	id("com.android.application")
	kotlin("android")
	id("com.mikepenz.aboutlibraries.plugin.android")
	id("com.github.breadmoirai.github-release")
}

kotlin {
	jvmToolchain(libs.versions.jvmToolchain.get().toInt())
}

android {
	buildToolsVersion = libs.versions.buildToolsVersion.get()
	compileSdk = libs.versions.compileSdk.get().toInt()
	namespace = "stef40.${name.lowercase()}"
	testNamespace = "$namespace.test"
	testBuildType = getPropertyValue("test.build.type") ?: "debug"

	buildFeatures {
		buildConfig = true
	}

	defaultConfig {
		minSdk = libs.versions.minSdk.get().toInt()
		targetSdk = compileSdk
		versionCode = (getPropertyValue("version.code") ?: libs.versions.versionCode.get()).toInt()
		versionName = getPropertyValue("version.name") ?: "$versionCode"
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		buildConfigField(
			"String",
			"LATEST_RELEASE_URL",
			"\"${getPropertyValue("latest.release.url") ?: ""}\""
		)
		buildConfigField(
			"String",
			"PROJECT_NAME",
			"\"${rootProject.name}\""
		)
	}

	signingConfigs {

		named("debug") {
			storeFile = rootProject.file(getPropertyValue("store.file") ?: return@named)
			storePassword = getPropertyValue("store.password") ?: return@named
			keyAlias = getPropertyValue("key.alias") ?: return@named
			keyPassword = getPropertyValue("key.password") ?: return@named
		}
	}

	buildTypes {

		named("release") {
			val isNotTestBuildType = testBuildType != name
			isMinifyEnabled = isNotTestBuildType
			isShrinkResources = isNotTestBuildType
			if (isNotTestBuildType) {
				proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
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
	implementation(project(":about"))
	implementation(project(":base"))
	getPropertyValue("latest.release.url")?.let { implementation(project(":update")) }
	androidTestImplementation(libs.androidTest.runner)
	androidTestImplementation(libs.androidTest.junit)
	androidTestImplementation(libs.androidTest.uiautomator)
	testImplementation(libs.test.junit)
}

getPropertyValue("adb.args")?.let {
	tasks.register<Exec>("adb") {
		group = rootProject.name
		executable = android.adbExecutable.path
		args(*Commandline.translateCommandline(it))

		doFirst {
			println("adb ${args.joinToString(" ")}")
		}
	}
}

tasks.githubRelease {
	val packageRelease = tasks.named<PackageApplication>("packageRelease")
	dependsOn(packageRelease)
	owner = getPropertyValue("github.owner")
	repo = getPropertyValue("github.repo")
	authorization = getPropertyValue("github.authorization")
	tagName = "v${android.defaultConfig.versionName}"
	targetCommitish = getPropertyValue("github.targetCommitish")
	releaseName = android.defaultConfig.versionName
	body = getPropertyValue("github.body")
	prerelease = true
	releaseAssets.from(packageRelease.map { task ->
		task.outputDirectory.asFileTree.filter { it.extension == "apk" }.sorted()
	})
}
