package ilchev.stefan.callblocker

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast

class CallReceiver : BroadcastReceiver() {

	override fun onReceive(context: Context?, intent: Intent?) {
		context ?: return
		val phoneNumber = intent?.incomingNumber ?: return
		if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED ||
			intent.getStringExtra(TelephonyManager.EXTRA_STATE) != TelephonyManager.EXTRA_STATE_RINGING
		) return
		val sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
		try {
			val blockPredicate = BlockPredicate(sharedPreferences)
			if (blockPredicate(phoneNumber, context.contentResolver) && endCall(context)) {
				notifyBlockedCall(context, phoneNumber)
			}
		} catch (t: Throwable) {
			Log.w(TAG, t)
		}
	}

	companion object {

		private const val TAG = "CallReceiver"

		private const val ANSWER_PHONE_CALLS = Manifest.permission.ANSWER_PHONE_CALLS

		private var notificationId = 0

		@Suppress("deprecation")
		private val Intent.incomingNumber
			get() = getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

		@Suppress("deprecation")
		private fun endCall(
			context: Context
		) = if (context.checkSelfPermission(ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED) {
			context.getSystemService(TelecomManager::class.java)?.endCall() == true
		} else {
			Toast.makeText(context, ANSWER_PHONE_CALLS, Toast.LENGTH_LONG).show()
			false
		}

		fun notifyBlockedCall(context: Context, phoneNumber: String?) {
			val manager = context.getSystemService(NotificationManager::class.java) ?: return
			notificationId = notificationId % Int.MAX_VALUE + 1
			val id = notificationId
			val applicationInfo = context.applicationInfo
			val applicationIcon = applicationInfo.icon
			val applicationLabel = context.packageManager.getApplicationLabel(applicationInfo)
			if (id == 1) {
				val channel = NotificationChannel(
					BuildConfig.APPLICATION_ID,
					applicationLabel,
					NotificationManager.IMPORTANCE_MIN
				)
				manager.createNotificationChannel(channel)
			}
			phoneNumber ?: return
			val text = context.getString(R.string.blocked_phone_number, phoneNumber)
			val intent = PendingIntent.getActivity(
				context,
				id,
				Intent(Intent.ACTION_MAIN, null, context, MainActivity::class.java)
					.addCategory(Intent.CATEGORY_LAUNCHER),
				PendingIntent.FLAG_IMMUTABLE
			)
			val builder = Notification.Builder(context, BuildConfig.APPLICATION_ID)
				.setSmallIcon(applicationIcon)
				.setContentTitle(applicationLabel)
				.setContentText(text)
				.setContentIntent(intent)
			manager.notify(id, builder.build())
		}
	}
}
