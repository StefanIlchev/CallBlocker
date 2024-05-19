package ilchev.stefan.callblocker.test

import ilchev.stefan.callblocker.BuildConfig
import org.junit.After
import org.junit.Assert
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import java.time.Duration
import java.time.Instant

class UpdateTest {

	private var isInstalled = false

	@Before
	fun before() {
		val buildType = if (BuildConfig.DEBUG) "release" else "debug"
		val task = "assemble${buildType.replaceFirstChar { it.uppercaseChar() }}"
		val fileName = "${BuildConfig.PROJECT_NAME}-$buildType-$VERSION_NAME.apk"
		val filePath = "build/outputs/apk/$buildType/$fileName"
		val build = "$task -p ${BuildConfig.PROJECT_NAME} -Dversion.name=\"$VERSION_NAME\""
		val install = "install -g $filePath"
		isInstalled = BuildConfig.DEBUG &&
				executeGradle(build) &&
				executeAdb(install)
	}

	@After
	fun after() {
		if (isInstalled) {
			val uninstall = "uninstall ${BuildConfig.APPLICATION_ID}"
			executeAdb(uninstall)
		}
	}

	@Test
	fun test() {
		Assume.assumeTrue(isInstalled)
		val start = listOf(
			"appops set --uid ${BuildConfig.APPLICATION_ID} REQUEST_INSTALL_PACKAGES allow",
			"am start -W -S ${BuildConfig.APPLICATION_ID}/.MainActivity"
		).joinToString(" && ", "shell ")
		Assert.assertTrue(executeAdb(start))
		assertUpdate()
	}

	companion object {

		private const val VERSION_NAME = "update.test"

		private fun toCheck(
			versionName: String
		) = listOf(
			"dumpsys package ${BuildConfig.APPLICATION_ID}",
			"grep 'versionName=$versionName'"
		).joinToString(" | ", "shell ")

		private fun assertUpdate() {
			val check = toCheck(VERSION_NAME)
			val range = Instant.now().let { it..it + Duration.ofMinutes(10L) }
			Assume.assumeFalse(BuildConfig.LATEST_RELEASE_URL.isEmpty())
			while (executeAdb(check)) {
				Assert.assertTrue(Instant.now() in range)
				Thread.sleep(1_000L)
			}
			Assert.assertTrue(executeAdb(toCheck(BuildConfig.VERSION_NAME)))
		}
	}
}
