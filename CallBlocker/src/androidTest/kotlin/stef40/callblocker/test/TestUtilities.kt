package stef40.callblocker.test

import android.app.Instrumentation
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import stef40.base.getPackageInfo
import java.util.Scanner

private const val TAG = "TestUtilities"

const val TIMEOUT = 5_000L

val instrumentation: Instrumentation
	get() = InstrumentationRegistry.getInstrumentation()

val targetContext: Context
	get() = instrumentation.targetContext

private fun Instrumentation.executeShell(
	command: String
) = Scanner(ParcelFileDescriptor.AutoCloseInputStream(uiAutomation.executeShellCommand(command))).use {
	while (it.hasNextLine()) {
		val line = it.nextLine()
		Log.v(TAG, line)
	}
}

private fun Instrumentation.tryGrantRuntimePermission(
	permission: String
) = try {
	uiAutomation.grantRuntimePermission(targetContext.packageName, permission)
} catch (_: Throwable) {
}

fun Instrumentation.grantRequestedPermissions() {
	val packageInfo = targetContext.getPackageInfo(PackageManager.GET_PERMISSIONS)
	packageInfo.requestedPermissions?.forEach(::tryGrantRuntimePermission)
	executeShell("appops set ${targetContext.packageName} REQUEST_INSTALL_PACKAGES allow")
}

fun String.toBy() = By.res(targetContext.packageName, this)
