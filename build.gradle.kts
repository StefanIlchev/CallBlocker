plugins {
	id("com.android.application") apply false
	kotlin("android") apply false
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
