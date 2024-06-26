package ilchev.stefan.callblocker.test

import android.app.Instrumentation
import android.content.pm.PackageManager
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import ilchev.stefan.callblocker.getPackageInfo

const val TIMEOUT = 5_000L

private fun Instrumentation.executeAllowCmd(
	permission: String
) = uiAutomation.executeShellCommand("appops set --uid ${targetContext.packageName} $permission allow").close()

fun Instrumentation.grantRequestedPermissions() {
	executeAllowCmd("REQUEST_INSTALL_PACKAGES")
	targetContext.getPackageInfo(PackageManager.GET_PERMISSIONS).requestedPermissions?.forEach {
		try {
			uiAutomation.grantRuntimePermission(targetContext.packageName, it)
		} catch (ignored: Throwable) {
		}
	}
}

fun String.toBy() = By.res(InstrumentationRegistry.getInstrumentation().targetContext.packageName, this)
