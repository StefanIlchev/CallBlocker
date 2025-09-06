plugins {
	`kotlin-dsl`
}

kotlin {
	jvmToolchain(libs.versions.jvmToolchain.get().toInt())
}

allprojects {

	repositories {
		google()
		mavenCentral()
		mavenLocal()
	}

	tasks.withType<JavaCompile>().configureEach {
		options.isDeprecation = true
	}
}
