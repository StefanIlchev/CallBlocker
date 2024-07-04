package ilchev.stefan.callblocker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log

class CallReceiver : BroadcastReceiver() {

	private fun onActiveNumbersChange(context: Context?) {
		if (BuildConfig.DEBUG) {
			Log.v(TAG, "$activeNumbers")
		}
		mainHandler.removeCallbacksAndMessages(activeNumbers)
		context ?: return
		if (!activeNumbers[TelephonyManager.EXTRA_STATE_OFFHOOK].isNullOrEmpty()) return
		val phoneNumber = activeNumbers.firstNotNullOfOrNull { it.value.lastOrNull() } ?: return
		val sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)
		try {
			val blockPredicate = BlockPredicate(sharedPreferences, context::isContact)
			if (blockPredicate(phoneNumber)) {
				mainHandler.postDelayed({
					if (endCall(context)) {
						context.notifyBlockedCall(phoneNumber)
					}
				}, activeNumbers, 1L)
			}
		} catch (t: Throwable) {
			Log.w(TAG, t)
		}
	}

	override fun onReceive(context: Context?, intent: Intent?) {
		val incomingNumber = intent?.takeIf {
			it.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED
		}?.incomingNumber ?: return
		val isRemove = activeNumbers.filterValues { it.remove(incomingNumber) }.isNotEmpty()
		val isAdd = activeNumbers[intent.state]?.add(incomingNumber) == true
		if (isRemove || isAdd) {
			onActiveNumbersChange(context)
		}
	}

	companion object {

		private const val TAG = "CallReceiver"

		private val activeNumbers = mapOf<String, MutableSet<String>>(
			TelephonyManager.EXTRA_STATE_RINGING to mutableSetOf(),
			TelephonyManager.EXTRA_STATE_OFFHOOK to mutableSetOf()
		)
	}
}
