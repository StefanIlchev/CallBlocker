package stef40.callblocker.test

import stef40.callblocker.BuildConfig
import java.io.File

private val isWindows = "windows" in (System.getProperty("os.name")?.lowercase() ?: "")

fun executeGradle(
	args: String
) = if (isWindows) {
	ProcessBuilder("cmd", "/c", "gradlew $args")
} else {
	ProcessBuilder("sh", "-c", "sh gradlew $args")
}.directory(File("..")).inheritIO().start().waitFor() == 0

fun executeAdb(
	args: String
) = if (isWindows) {
	executeGradle("adb -p ${BuildConfig.PROJECT_NAME} -Dadb.args=\"$args\"")
} else {
	executeGradle("adb -p ${BuildConfig.PROJECT_NAME} -Dadb.args='$args'")
}
