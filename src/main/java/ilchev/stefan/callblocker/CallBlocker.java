package ilchev.stefan.callblocker;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class CallBlocker extends BroadcastReceiver {

	private static boolean isPrivateNumber(String phoneNumber) {
		return phoneNumber == null || phoneNumber.isEmpty();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (context == null || intent == null) {
			return;
		}
		String action = intent.getAction();
		if (!TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
			return;
		}
		String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
		if (!TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
			return;
		}
		if (!intent.hasExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)) {
			return;
		}
		String phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
		if (!isPrivateNumber(phoneNumber)) {
			return;
		}
		if (!context.getSharedPreferences(MainActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE)
				.getBoolean(MainActivity.IS_ENABLED, MainActivity.IS_ENABLED_DEFAULT)) {
			return;
		}
		if (context.checkSelfPermission(Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
			Toast.makeText(context, Manifest.permission.ANSWER_PHONE_CALLS, Toast.LENGTH_LONG)
					.show();
		}
		TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
		if (telecomManager != null && telecomManager.endCall()) {
			Toast.makeText(context, R.string.app_name, Toast.LENGTH_LONG)
					.show();
		}
	}
}
