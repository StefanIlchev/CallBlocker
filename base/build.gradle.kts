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

	defaultConfig {
		minSdk = libs.versions.minSdk.get().toInt()
	}
}
