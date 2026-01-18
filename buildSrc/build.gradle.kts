plugins {
	`kotlin-dsl`
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
