import stef40.buildsrc.getPropertyValue

plugins {
	id("com.android.library")
	kotlin("android")
}

kotlin {
	jvmToolchain(libs.versions.jvmToolchain.get().toInt())
}

android {
	buildToolsVersion = libs.versions.buildToolsVersion.get()
	compileSdk = libs.versions.compileSdk.get().toInt()
	namespace = "stef40.${name.lowercase()}"

	buildFeatures {
		buildConfig = true
	}

	defaultConfig {
		minSdk = libs.versions.minSdk.get().toInt()
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
}

dependencies {
	implementation(project(":base"))
}
