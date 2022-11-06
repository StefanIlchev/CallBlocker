package ilchev.stefan.callblocker;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class CallReceiver extends BroadcastReceiver {

	private static final String TAG = "CallReceiver";

	private static int NOTIFICATION_ID = 0;

	@SuppressWarnings({"deprecation", "RedundantSuppression"})
	private static String getIncomingNumber(Intent intent) {
		return intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
	}

	@SuppressWarnings({"deprecation", "RedundantSuppression"})
	private static boolean endCall(Context context) {
		if (context.checkSelfPermission(Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
			Toast.makeText(context, Manifest.permission.ANSWER_PHONE_CALLS, Toast.LENGTH_LONG).show();
			return false;
		}
		var telecomManager = context.getSystemService(TelecomManager.class);
		return telecomManager != null && telecomManager.endCall();
	}

	public static void notifyBlockedCall(Context context, String phoneNumber) {
		var manager = context.getSystemService(NotificationManager.class);
		if (manager == null) {
			return;
		}
		var id = NOTIFICATION_ID = NOTIFICATION_ID % Integer.MAX_VALUE + 1;
		var applicationInfo = context.getApplicationInfo();
		var applicationIcon = applicationInfo.icon;
		var applicationLabel = context.getPackageManager().getApplicationLabel(applicationInfo);
		if (id == 1) {
			var channel = new NotificationChannel(
					BuildConfig.APPLICATION_ID,
					applicationLabel,
					NotificationManager.IMPORTANCE_MIN);
			manager.createNotificationChannel(channel);
		}
		if (phoneNumber == null) {
			return;
		}
		var text = context.getString(R.string.blocked_phone_number, phoneNumber);
		var intent = PendingIntent.getActivity(
				context,
				id,
				new Intent(Intent.ACTION_MAIN, null, context, MainActivity.class)
						.addCategory(Intent.CATEGORY_LAUNCHER),
				PendingIntent.FLAG_IMMUTABLE);
		var builder = new Notification.Builder(context, BuildConfig.APPLICATION_ID)
				.setSmallIcon(applicationIcon)
				.setContentTitle(applicationLabel)
				.setContentText(text)
				.setContentIntent(intent);
		manager.notify(id, builder.build());
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		var phoneNumber = intent != null ? getIncomingNumber(intent) : null;
		if (context == null || phoneNumber == null ||
				!TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction()) ||
				!TelephonyManager.EXTRA_STATE_RINGING.equals(intent.getStringExtra(TelephonyManager.EXTRA_STATE))) {
			return;
		}
		var sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
		try {
			var callPredicate = new CallPredicate(sharedPreferences);
			if (callPredicate.test(phoneNumber) &&
					endCall(context)) {
				notifyBlockedCall(context, phoneNumber);
			}
		} catch (Throwable t) {
			Log.w(TAG, t);
		}
	}
}
