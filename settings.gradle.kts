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
		id("com.android.application") version "8.1.0"

		// https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-stdlib
		kotlin("android") version "1.9.0"
	}
}