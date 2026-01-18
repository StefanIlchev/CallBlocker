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

	buildFeatures {
		buildConfig = true
	}
}

dependencies {
	implementation(project(":base"))
	implementation(libs.aboutlibraries)
}
