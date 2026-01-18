import stef40.buildsrc.getPropertyValue

plugins {
	alias(libs.plugins.android.library)
}

android {
	namespace = "stef40.${name.lowercase()}"
	buildToolsVersion = libs.versions.buildToolsVersion.get()

	compileSdk {
		version = release(libs.versions.compileSdk.get().toInt())
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

	buildFeatures {
		buildConfig = true
	}
}

dependencies {
	implementation(project(":base"))
}
