import java.util.Properties

val localProperties by ext(Properties().also {
	file("local.properties").takeIf(File::isFile)?.bufferedReader()?.use(it::load)
})

subprojects {

	repositories {
		google()
		mavenCentral()
		mavenLocal()
		maven { url = uri("https://jitpack.io") }
	}

	ext["localProperties"] = localProperties

	tasks.withType<JavaCompile>().configureEach {
		options.isDeprecation = true
	}
}
