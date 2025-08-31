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
		id("com.android.application") version "8.12.2"

		// https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-stdlib
		kotlin("android") version "2.2.10"

		// https://mvnrepository.com/artifact/com.mikepenz.aboutlibraries.plugin/com.mikepenz.aboutlibraries.plugin.gradle.plugin
		id("com.mikepenz.aboutlibraries.plugin") version "12.2.4"

		// https://mvnrepository.com/artifact/com.github.breadmoirai.github-release/com.github.breadmoirai.github-release.gradle.plugin
		id("com.github.breadmoirai.github-release") version "2.5.2"
	}
}

dependencyResolutionManagement {

	versionCatalogs {

		create("libs") {

			version("versionCode", "38")

			// https://developer.android.com/build/jdks
			version("jvmToolchain", "21")

			// https://developer.android.com/tools/releases/build-tools
			version("buildToolsVersion", "36.0.0")

			// https://developer.android.com/tools/releases/platforms
			version("compileSdk", "36")
			version("minSdk", "28")

			// https://mvnrepository.com/artifact/com.mikepenz/aboutlibraries
			library("aboutlibraries", "com.mikepenz:aboutlibraries:12.2.4")

			// https://mvnrepository.com/artifact/androidx.test/runner
			library("androidTest.runner", "androidx.test:runner:1.7.0")

			// https://mvnrepository.com/artifact/androidx.test.ext/junit-ktx
			library("androidTest.junit", "androidx.test.ext:junit-ktx:1.3.0")

			// https://mvnrepository.com/artifact/androidx.test.uiautomator/uiautomator
			library("androidTest.uiautomator", "androidx.test.uiautomator:uiautomator:2.3.0")

			// https://mvnrepository.com/artifact/junit/junit
			library("test.junit", "junit:junit:4.13.2")
		}
	}
}

include(":CallBlocker")
