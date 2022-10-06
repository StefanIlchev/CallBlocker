package ilchev.stefan.callblocker;

import android.os.Build;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.util.Log;

public class CallService extends CallScreeningService {

	private static final String TAG = "CallService";

	private static void endCall(CallResponse.Builder builder) {
		builder.setDisallowCall(true)
				.setRejectCall(true)
				.setSkipNotification(true);
	}

	@Override
	public void onScreenCall(Call.Details callDetails) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
				callDetails.getCallDirection() != Call.Details.DIRECTION_INCOMING) {
			return;
		}
		var builder = new CallResponse.Builder();
		try {
			var phoneNumber = callDetails.getHandle().getSchemeSpecificPart();
			var sharedPreferences = getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE);
			var callBlocker = new CallBlocker(sharedPreferences);
			if (callBlocker.isBlocked(phoneNumber)) {
				endCall(builder);
				CallReceiver.notifyBlockedCall(this, phoneNumber);
			}
		} catch (Throwable t) {
			Log.w(TAG, t);
		} finally {
			respondToCall(callDetails, builder.build());
		}
	}
}
