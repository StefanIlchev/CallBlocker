package ilchev.stefan.callblocker

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log

class CallService : CallScreeningService() {

	override fun onScreenCall(callDetails: Call.Details) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
			callDetails.callDirection != Call.Details.DIRECTION_INCOMING
		) return
		val builder = CallResponse.Builder()
		try {
			val phoneNumber = callDetails.handle.schemeSpecificPart
			val sharedPreferences = getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE)
			val blockPredicate = BlockPredicate(sharedPreferences)
			if (blockPredicate(phoneNumber)) {
				endCall(builder)
				CallReceiver.notifyBlockedCall(this, phoneNumber)
			}
		} catch (t: Throwable) {
			Log.w(TAG, t)
		} finally {
			respondToCall(callDetails, builder.build())
		}
	}

	companion object {

		private const val TAG = "CallService"

		private fun endCall(
			builder: CallResponse.Builder
		) = builder.setDisallowCall(true)
			.setRejectCall(true)
			.setSkipNotification(true)
	}
}
