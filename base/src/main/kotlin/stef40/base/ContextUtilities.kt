package stef40.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets

private const val TAG = "ContextUtilities"

const val NOTIFICATION_ID = Int.MAX_VALUE

val mainHandler = Handler(Looper.getMainLooper())

val updateServiceType = try {
	Class.forName("stef40.update.UpdateService")
} catch (_: Throwable) {
	null
}

val Context.usableUpdateServiceType
	get() = updateServiceType?.takeIf { packageManager.canRequestPackageInstalls() }

val Context.sharedPreferences: SharedPreferences
	get() = getSharedPreferences(packageName, Context.MODE_PRIVATE)

@Suppress("deprecation", "KotlinRedundantDiagnosticSuppress")
fun Context.getPackageInfo(
	flags: Int = 0
): PackageInfo = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
	packageManager.getPackageInfo(packageName, flags)
} else {
	packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
}

val Context.requestedPermissions
	get() = getPackageInfo(PackageManager.GET_PERMISSIONS).requestedPermissions ?: emptyArray()

fun Context.isActivityFound(
	intent: Intent
) = try {
	val activityInfo = intent.resolveActivityInfo(packageManager, 0)
	activityInfo?.isEnabled == true && activityInfo.exported
} catch (t: Throwable) {
	Log.w(TAG, t)
	false
}

fun Context.tryStartActivity(intent: Intent, options: Bundle? = null) {
	try {
		startActivity(intent, options)
	} catch (t: Throwable) {
		Log.w(TAG, t)
	}
}

fun Activity.tryStartActivityForResult(intent: Intent, requestCode: Int = -1, options: Bundle? = null) {
	try {
		startActivityForResult(intent, requestCode, options)
	} catch (t: Throwable) {
		Log.w(TAG, t)
	}
}

fun Context.tryStartForegroundService(intent: Intent) {
	try {
		startForegroundService(intent)
	} catch (t: Throwable) {
		Log.w(TAG, t)
	}
}

fun Context.tryStopService(
	intent: Intent
) = try {
	stopService(intent)
} catch (t: Throwable) {
	Log.w(TAG, t)
	false
}

@Suppress("deprecation")
fun <T : Parcelable> getParcelableExtra(
	intent: Intent,
	name: String,
	clazz: Class<T>
) = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
	intent.getParcelableExtra(name)
} else {
	intent.getParcelableExtra(name, clazz)
}

@SuppressLint("UnspecifiedRegisterReceiverFlag")
fun Context.registerExportedReceiver(
	receiver: BroadcastReceiver,
	filter: IntentFilter
) = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
	registerReceiver(receiver, filter, null, mainHandler)
} else {
	registerReceiver(receiver, filter, null, mainHandler, Context.RECEIVER_EXPORTED)
}

val applyWindowInsetsListener = View.OnApplyWindowInsetsListener { v, insets ->
	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return@OnApplyWindowInsetsListener insets
	v.layoutParams = (v.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
		val systemBars = insets.getInsets(WindowInsets.Type.systemBars())
		leftMargin = systemBars.left
		topMargin = systemBars.top
		rightMargin = systemBars.right
		bottomMargin = systemBars.bottom
	} ?: return@OnApplyWindowInsetsListener insets
	WindowInsets.CONSUMED
}
