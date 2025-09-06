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
		val gitHubUrl = getPropertyValue("github.owner")?.let { owner ->
			getPropertyValue("github.repo")?.let { repo ->
				"https://github.com/$owner/$repo"
			}
		} ?: ""
		buildConfigField("String", "GIT_HUB_URL", "\"$gitHubUrl\"")
		buildConfigField(
			"String",
			"SPONSOR_URL",
			"\"${getPropertyValue("sponsor.url") ?: ""}\""
		)
	}
}

dependencies {
	implementation(project(":base"))
	implementation(libs.aboutlibraries)
}
