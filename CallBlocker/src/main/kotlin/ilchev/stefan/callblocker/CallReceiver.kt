package ilchev.stefan.callblocker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log

class CallReceiver : BroadcastReceiver() {

	override fun onReceive(context: Context?, intent: Intent?) {
		context ?: return
		val phoneNumber = intent?.incomingNumber ?: return
		if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED ||
			intent.getStringExtra(TelephonyManager.EXTRA_STATE) != TelephonyManager.EXTRA_STATE_RINGING
		) return
		val sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
		try {
			val blockPredicate = BlockPredicate(sharedPreferences, context::isContact)
			if (blockPredicate(phoneNumber) && endCall(context)) {
				context.notifyBlockedCall(phoneNumber)
			}
		} catch (t: Throwable) {
			Log.w(TAG, t)
		}
	}

	companion object {

		private const val TAG = "CallReceiver"
	}
}
