import java.util.Properties

plugins {
	alias(libs.plugins.android.application) apply false
	alias(libs.plugins.android.library) apply false
}

val localProperties = Properties().also {
	file("local.properties").takeIf(File::isFile)?.bufferedReader()?.use(it::load)
}

val getPropertyValue = { key: String ->
	(System.getProperty(key) ?: localProperties.getProperty(key))?.ifEmpty { null }
}.also { extra["getPropertyValue"] = it }

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
