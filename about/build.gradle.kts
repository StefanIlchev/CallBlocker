plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.aboutlibraries)
}

@Suppress("UNCHECKED_CAST")
val getPropertyValue = rootProject.extra["getPropertyValue"] as (String) -> String?

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
		compose = true
	}
}

dependencies {
	implementation(project(":base"))
	implementation(libs.androidx.activity.compose)
	implementation(libs.androidx.compose.material3)
	implementation(libs.androidx.compose.material.icons)
	implementation(libs.androidx.compose.ui)
	implementation(libs.aboutlibraries)
	debugImplementation(libs.androidx.compose.ui.tooling.preview)
}
