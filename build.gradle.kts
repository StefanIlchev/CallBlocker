import java.util.Properties

plugins {
	alias(libs.plugins.android.application) apply false
	alias(libs.plugins.android.library) apply false
}

val localProperties by extra {
	Properties().also { file("local.properties").takeIf(File::isFile)?.bufferedReader()?.use(it::load) }
}

val getPropertyValue by extra { key: String ->
	(System.getProperty(key) ?: localProperties.getProperty(key))?.ifEmpty { null }
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
