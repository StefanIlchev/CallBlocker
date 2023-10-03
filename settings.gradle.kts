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
		id("com.android.application") version "8.1.2"

		// https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-stdlib
		kotlin("android") version "1.9.10"
	}
}

dependencyResolutionManagement {

	versionCatalogs {

		create("libs") {

			// https://developer.android.com/tools/releases/build-tools
			version("buildToolsVersion", "33.0.2")

			// https://developer.android.com/tools/releases/platforms
			version("compileSdk", "33")
			version("minSdk", "28")
		}
	}
}
