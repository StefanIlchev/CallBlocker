package ilchev.stefan.callblocker;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

@SuppressWarnings({"deprecation", "RedundantSuppression"})
public class CallReceiver extends BroadcastReceiver {

	private static final String TAG = "CallReceiver";

	private static void endCall(Context context, String phoneNumber) {
		if (context.checkSelfPermission(Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
			Toast.makeText(context, Manifest.permission.ANSWER_PHONE_CALLS, Toast.LENGTH_LONG).show();
		}
		var telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
		if (telecomManager != null && telecomManager.endCall()) {
			var text = context.getString(R.string.blocked_phone_number, phoneNumber);
			Toast.makeText(context, text, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		var phoneNumber = intent != null ? intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) : null;
		if (context == null || phoneNumber == null ||
				!TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction()) ||
				!TelephonyManager.EXTRA_STATE_RINGING.equals(intent.getStringExtra(TelephonyManager.EXTRA_STATE))) {
			return;
		}
		var sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
		try {
			var callBlocker = new CallBlocker(sharedPreferences);
			if (callBlocker.isBlocked(phoneNumber)) {
				endCall(context, phoneNumber);
			}
		} catch (Throwable t) {
			Log.w(TAG, t);
		}
	}
}
