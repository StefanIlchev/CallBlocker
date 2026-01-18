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
	}
}
