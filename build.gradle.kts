plugins {
	alias(libs.plugins.android.application) apply false
	alias(libs.plugins.android.library) apply false
}

subprojects {

	repositories {
		google()
		mavenCentral()
		mavenLocal()
	}

	tasks.withType<JavaCompile>().configureEach {
		options.isDeprecation = true
	}
}
