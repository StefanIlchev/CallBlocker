package ilchev.stefan.callblocker

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

private const val TAG = "CallUtilities"

@Suppress("deprecation")
val Intent.incomingNumber
	get() = getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

val Call.Details.isContact
	get() = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) null else contactDisplayName?.run { true }

private fun ContentResolver.isContact(
	phoneNumber: String
) = try {
	val task = Callable {
		query(
			Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber)),
			arrayOf(ContactsContract.PhoneLookup._ID),
			null,
			null,
			null
		)?.use { it.moveToFirst() }
	}
	val factory = Executors.defaultThreadFactory()
	val executor = Executors.newSingleThreadExecutor {
		factory.newThread(it).apply {
			if (!isDaemon) {
				isDaemon = true
			}
		}
	}
	val future = try {
		executor.submit(task)
	} finally {
		executor.shutdown()
	}
	try {
		future.get(3_000L, TimeUnit.MILLISECONDS)
	} catch (e: TimeoutException) {
		future.cancel(true)
		throw e
	}
} catch (t: Throwable) {
	Log.w(TAG, t)
	null
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
	blockedCallNotificationId = blockedCallNotificationId % Int.MAX_VALUE + 1
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
