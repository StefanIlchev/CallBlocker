package ilchev.stefan.callblocker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log

class CallReceiver : BroadcastReceiver() {

	private fun onRingingNumbersChanged(context: Context) {
		mainHandler.removeCallbacksAndMessages(ringingNumbers)
		val phoneNumber = ringingNumbers.lastOrNull() ?: return
		val sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
		try {
			val blockPredicate = BlockPredicate(sharedPreferences, context::isContact)
			if (blockPredicate(phoneNumber)) {
				mainHandler.postDelayed({
					if (endCall(context)) {
						context.notifyBlockedCall(phoneNumber)
					}
				}, ringingNumbers, 1L)
			}
		} catch (t: Throwable) {
			Log.w(TAG, t)
		}
	}

	override fun onReceive(context: Context?, intent: Intent?) {
		context ?: return
		val incomingNumber = intent?.takeIf {
			it.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED
		}?.incomingNumber ?: return
		val isRemoved = ringingNumbers.remove(incomingNumber)
		if (intent.state == TelephonyManager.EXTRA_STATE_RINGING && ringingNumbers.add(incomingNumber) || isRemoved) {
			onRingingNumbersChanged(context)
		}
	}

	companion object {

		private const val TAG = "CallReceiver"

		private val ringingNumbers = mutableSetOf<String>()
	}
}
