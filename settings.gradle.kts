pluginManagement {

	repositories {
		google()
		mavenCentral()
		mavenLocal()
		maven("https://jitpack.io")
		gradlePluginPortal()
	}

	plugins {

		// https://mvnrepository.com/artifact/com.android.tools.build/gradle
		id("com.android.application") version "8.11.1"

		// https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-stdlib
		kotlin("android") version "2.2.0"

		// https://plugins.gradle.org/plugin/com.github.breadmoirai.github-release
		id("com.github.breadmoirai.github-release") version "2.5.2"
	}
}

dependencyResolutionManagement {

	versionCatalogs {

		create("libs") {

			version("versionCode", "34")

			// https://developer.android.com/build/jdks
			version("jvmToolchain", "21")

			// https://developer.android.com/tools/releases/build-tools
			version("buildToolsVersion", "36.0.0")

			// https://developer.android.com/tools/releases/platforms
			version("compileSdk", "36")
			version("minSdk", "28")

			// https://mvnrepository.com/artifact/androidx.test/runner
			library("androidTest.runner", "androidx.test:runner:1.6.2")

			// https://mvnrepository.com/artifact/androidx.test.ext/junit-ktx
			library("androidTest.junit", "androidx.test.ext:junit-ktx:1.2.1")

			// https://mvnrepository.com/artifact/androidx.test.uiautomator/uiautomator
			library("androidTest.uiautomator", "androidx.test.uiautomator:uiautomator:2.3.0")

			// https://mvnrepository.com/artifact/junit/junit
			library("test.junit", "junit:junit:4.13.2")
		}
	}
}

include(":CallBlocker")
