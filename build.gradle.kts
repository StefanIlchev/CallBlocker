allprojects {

	repositories {
		google()
		mavenCentral()
		mavenLocal()
		maven { url = uri("https://jitpack.io") }
	}

	tasks.withType<JavaCompile>().configureEach {
		options.isDeprecation = true
	}
}
