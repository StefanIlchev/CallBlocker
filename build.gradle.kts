import java.util.Properties

val localProperties = Properties().also {
	file("local.properties").takeIf(File::isFile)?.bufferedReader()?.use(it::load)
}

subprojects {

	repositories {
		google()
		mavenCentral()
		mavenLocal()
		maven("https://jitpack.io")
	}

	extra["localProperties"] = localProperties

	tasks.withType<JavaCompile>().configureEach {
		options.isDeprecation = true
	}
}
