import java.util.Properties

@Suppress("unused")
val localProperties by extra {
	Properties().also { file("local.properties").takeIf(File::isFile)?.bufferedReader()?.use(it::load) }
}

subprojects {

	repositories {
		google()
		mavenCentral()
		mavenLocal()
		maven("https://jitpack.io")
	}

	tasks.withType<JavaCompile>().configureEach {
		options.isDeprecation = true
	}
}
