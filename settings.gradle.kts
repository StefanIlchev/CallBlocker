pluginManagement {

	repositories {
		google()
		mavenCentral()
		mavenLocal()
		maven { url = uri("https://jitpack.io") }
		gradlePluginPortal()
	}

	plugins {

		// https://mvnrepository.com/artifact/com.android.tools.build/gradle?repo=google
		id("com.android.application") version "8.3.0"

		// https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-stdlib
		kotlin("android") version "1.9.22"
	}
}

dependencyResolutionManagement {

	versionCatalogs {

		create("libs") {

			// https://developer.android.com/build/jdks
			version("jvmToolchain", "17")

			// https://developer.android.com/tools/releases/build-tools
			version("buildToolsVersion", "34.0.0")

			// https://developer.android.com/tools/releases/platforms
			version("compileSdk", "34")
			version("minSdk", "28")
		}
	}
}

include(":CallBlocker")
