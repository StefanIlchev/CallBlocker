import com.android.build.api.variant.impl.VariantOutputImpl
import com.android.build.gradle.tasks.PackageApplication
import com.mikepenz.aboutlibraries.plugin.AboutLibrariesTask
import org.ajoberstar.grgit.Grgit
import org.apache.tools.ant.types.Commandline
import stef40.buildsrc.getPropertyValue

plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.aboutlibraries)
	alias(libs.plugins.grgit)
	alias(libs.plugins.github.release)
}

android {
	namespace = "stef40.${name.lowercase()}"
	testNamespace = "$namespace.test"
	testBuildType = getPropertyValue("test.build.type") ?: "debug"
	buildToolsVersion = libs.versions.buildToolsVersion.get()

	compileSdk {
		version = release(libs.versions.compileSdk.get().toInt())
	}

	defaultConfig {
		minSdk = libs.versions.minSdk.get().toInt()
		targetSdk = compileSdk
		versionCode = (getPropertyValue("version.code") ?: libs.versions.versionCode.get()).toInt()
		versionName = getPropertyValue("version.name") ?: "$versionCode"
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		buildConfigField(
			"String",
			"LATEST_RELEASE_URL",
			"\"${getPropertyValue("latest.release.url") ?: ""}\""
		)
		buildConfigField(
			"String",
			"PROJECT_NAME",
			"\"${rootProject.name}\""
		)
	}

	signingConfigs {

		named("debug") {
			storeFile = rootProject.file(getPropertyValue("store.file") ?: return@named)
			storePassword = getPropertyValue("store.password") ?: return@named
			keyAlias = getPropertyValue("key.alias") ?: return@named
			keyPassword = getPropertyValue("key.password") ?: return@named
		}
	}

	buildTypes {

		release {
			val isNotTestBuildType = testBuildType != name
			isMinifyEnabled = isNotTestBuildType
			isShrinkResources = isNotTestBuildType
			if (isNotTestBuildType) {
				proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
			}
			signingConfig = signingConfigs["debug"]
		}
	}

	buildFeatures {
		buildConfig = true
	}
}

androidComponents {

	onVariants { variant ->
		variant.outputs.forEach {
			if (it !is VariantOutputImpl) return@forEach
			it.outputFileName = file(it.outputFileName.get()).run {
				"$nameWithoutExtension-${it.versionName.get()}.$extension"
			}
		}

		abstract class AboutLibrariesResTask : AboutLibrariesTask() {

			@get:Optional
			@get:OutputDirectory
			abstract val outputDirectory: DirectoryProperty
		}

		val variantName = variant.name.replaceFirstChar { it.uppercase() }
		val resultsResDirectory = project.layout.buildDirectory.dir("generated/aboutLibraries/${variant.name}/res")
		val resultsDirectory = resultsResDirectory.map { it.dir("raw") }
		val task = project.tasks.register<AboutLibrariesResTask>("prepareLibraryDefinitions$variantName") {
			group = rootProject.name
			this.variant.set(variant.name)
			outputDirectory.set(resultsResDirectory)
			configureOutputFile(resultsDirectory.map { dir ->
				@Suppress("DEPRECATION")
				dir.file(aboutLibraries.export.outputFileName.get())
			})
			configure()
		}
		variant.sources.res?.addGeneratedSourceDirectory(task) { it.outputDirectory }
	}
}

dependencies {
	implementation(project(":about"))
	implementation(project(":base"))
	getPropertyValue("latest.release.url")?.let { implementation(project(":update")) }
	androidTestImplementation(libs.androidTest.runner)
	androidTestImplementation(libs.androidTest.junit)
	androidTestImplementation(libs.androidTest.uiautomator)
	testImplementation(libs.test.junit)
}

getPropertyValue("adb.args")?.let {
	tasks.register<Exec>("adb") {
		group = rootProject.name

		doFirst {
			executable(androidComponents.sdkComponents.adb.get())
			args(*Commandline.translateCommandline(it))
			println("adb ${args.joinToString(" ")}")
		}
	}
}

tasks.githubRelease {
	val packageRelease = tasks.named<PackageApplication>("packageRelease")
	dependsOn(packageRelease)
	owner = getPropertyValue("github.owner")
	repo = getPropertyValue("github.repo")
	authorization = getPropertyValue("github.authorization")
	tagName = "v${android.defaultConfig.versionName}"
	targetCommitish = Grgit.open(mapOf("dir" to rootDir.path)).branch.current().name
	releaseName = android.defaultConfig.versionName
	body = getPropertyValue("github.body")
	prerelease = true
	releaseAssets.from(packageRelease.map { task ->
		task.outputDirectory.asFileTree.filter { it.extension == "apk" }.sorted()
	})
}
