package ilchev.stefan.callblocker

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log

class CallService : CallScreeningService() {

	override fun onScreenCall(callDetails: Call.Details) {
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P &&
			callDetails.callDirection != Call.Details.DIRECTION_INCOMING
		) return
		val builder = CallResponse.Builder()
		try {
			val phoneNumber = callDetails.handle?.schemeSpecificPart ?: ""
			val isContact = callDetails.isContact
			val blockPredicate = BlockPredicate(sharedPreferences) { isContact ?: isContact(it) }
			if (blockPredicate(phoneNumber)) {
				endCall(builder)
				notifyBlockedCall(phoneNumber)
			}
		} catch (t: Throwable) {
			Log.w(TAG, t)
		} finally {
			respondToCall(callDetails, builder.build())
		}
	}

	companion object {

		private const val TAG = "CallService"
	}
}
