package ilchev.stefan.callblocker

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

private const val TAG = "CallUtilities"

val mainHandler = Handler(Looper.getMainLooper())

val Context.sharedPreferences: SharedPreferences
	get() = getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)

@Suppress("deprecation")
val Intent.incomingNumber
	get() = if (hasExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)) {
		getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) ?: ""
	} else {
		null
	}

val Intent.state
	get() = getStringExtra(TelephonyManager.EXTRA_STATE)

val Call.Details.isContact
	get() = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) null else contactDisplayName?.run { true }

private fun ContentResolver.isContact(phoneNumber: String): Boolean? {
	phoneNumber.ifEmpty { return null }
	val future = CompletableFuture.supplyAsync {
		query(
			Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber)),
			arrayOf(ContactsContract.PhoneLookup._ID),
			null,
			null,
			null
		)?.use(Cursor::moveToFirst)
	}
	try {
		return future.get(3_000L, TimeUnit.MILLISECONDS)
	} catch (e: TimeoutException) {
		future.cancel(true)
		Log.w(TAG, e)
	} catch (t: Throwable) {
		Log.w(TAG, t)
	}
	return null
}

fun Context.isContact(
	phoneNumber: String
) = if (checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
	contentResolver?.isContact(phoneNumber)
} else {
	Toast.makeText(this, Manifest.permission.READ_CONTACTS, Toast.LENGTH_LONG).show()
	null
}

@Suppress("deprecation")
fun endCall(
	context: Context
) = if (context.checkSelfPermission(Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED) {
	context.getSystemService(TelecomManager::class.java)?.endCall() == true
} else {
	Toast.makeText(context, Manifest.permission.ANSWER_PHONE_CALLS, Toast.LENGTH_LONG).show()
	false
}

fun endCall(
	builder: CallScreeningService.CallResponse.Builder
): CallScreeningService.CallResponse.Builder = builder.setDisallowCall(true)
	.setRejectCall(true)
	.setSkipNotification(true)

private var blockedCallNotificationId = 0

fun Context.notifyBlockedCall(phoneNumber: String?) {
	val manager = getSystemService(NotificationManager::class.java) ?: return
	blockedCallNotificationId = blockedCallNotificationId % (UpdateService.NOTIFICATION_ID - 1) + 1
	val id = blockedCallNotificationId
	val applicationInfo = applicationInfo
	val applicationIcon = applicationInfo.icon
	val applicationLabel = packageManager.getApplicationLabel(applicationInfo)
	if (id == 1) {
		val channel = NotificationChannel(
			BuildConfig.APPLICATION_ID,
			applicationLabel,
			NotificationManager.IMPORTANCE_MIN
		)
		manager.createNotificationChannel(channel)
	}
	phoneNumber ?: return
	val text = getString(R.string.blocked_phone_number, phoneNumber)
	val intent = PendingIntent.getActivity(
		this,
		id,
		Intent(Intent.ACTION_MAIN, null, this, MainActivity::class.java)
			.addCategory(Intent.CATEGORY_LAUNCHER),
		PendingIntent.FLAG_IMMUTABLE
	)
	val builder = Notification.Builder(this, BuildConfig.APPLICATION_ID)
		.setSmallIcon(applicationIcon)
		.setContentTitle(applicationLabel)
		.setContentText(text)
		.setContentIntent(intent)
	manager.notify(id, builder.build())
}

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

fun Context.tryStopService(
	intent: Intent
) = try {
	stopService(intent)
} catch (t: Throwable) {
	Log.w(TAG, t)
	false
}

@Suppress("deprecation", "KotlinRedundantDiagnosticSuppress")
fun Context.getPackageInfo(
	flags: Int = 0
): PackageInfo = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
	packageManager.getPackageInfo(packageName, flags)
} else {
	packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
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
