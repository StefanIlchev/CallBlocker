package ilchev.stefan.callblocker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";

	@SuppressLint("InlinedApi")
	private static final String POST_NOTIFICATIONS =
			Manifest.permission.POST_NOTIFICATIONS;

	private static int toBlockId(Boolean value) {
		return value == null ? R.id.block_none
				: value ? R.id.block_matches
				: R.id.block_others;
	}

	private static Boolean toBoolean(int value) {
		return value == R.id.block_matches ? Boolean.TRUE
				: value == R.id.block_others ? Boolean.FALSE
				: null;
	}

	private final TextWatcher regexListener = new TextWatcher() {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			var regex = s + "";
			updateContent(R.id.regex, it -> it.setRegex(regex));
		}
	};

	private final RadioGroup.OnCheckedChangeListener blockListener = (group, checkedId) -> {
		var isMatches = toBoolean(checkedId);
		updateContent(R.id.block, it -> it.setMatches(isMatches));
	};

	private final CompoundButton.OnCheckedChangeListener screenerListener = (buttonView, isChecked) ->
			updateScreener(buttonView);

	@SuppressWarnings({"deprecation", "RedundantSuppression"})
	private PackageInfo getPackageInfo(int flags) throws PackageManager.NameNotFoundException {
		var packageManager = getPackageManager();
		var packageName = getPackageName();
		return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
				? packageManager.getPackageInfo(packageName, flags)
				: packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags));
	}

	private void tryStartActivityForResult(Intent intent, int requestCode, Bundle options) {
		try {
			startActivityForResult(intent, requestCode, options);
		} catch (Throwable t) {
			Log.w(TAG, t);
		}
	}

	private void updateContent(int sourceId, Consumer<BlockPredicate> consumer) {
		var sharedPreferences = getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE);
		String error = null;
		try {
			var blockPredicate = new BlockPredicate(sharedPreferences);
			if (consumer != null) {
				consumer.accept(blockPredicate);
				blockPredicate.put(sharedPreferences.edit()).apply();
			}
			EditText regexView = sourceId != R.id.regex ? findViewById(R.id.regex) : null;
			if (regexView != null) {
				regexView.removeTextChangedListener(regexListener);
				regexView.setText(blockPredicate.getRegex());
				regexView.addTextChangedListener(regexListener);
			}
			RadioGroup blockView = sourceId != R.id.block ? findViewById(R.id.block) : null;
			if (blockView != null) {
				blockView.setOnCheckedChangeListener(null);
				blockView.check(toBlockId(blockPredicate.isMatches()));
				blockView.setOnCheckedChangeListener(blockListener);
			}
		} catch (Throwable t) {
			error = t.getLocalizedMessage();
		} finally {
			TextView errorView = findViewById(R.id.error);
			if (errorView != null) {
				errorView.setText(error);
			}
		}
	}

	private void updateScreener(CompoundButton screener, int visibility, boolean isChecked) {
		screener.setVisibility(visibility);
		screener.setOnCheckedChangeListener(null);
		screener.setChecked(isChecked);
		screener.setOnCheckedChangeListener(screenerListener);
	}

	private void updateScreener(CompoundButton buttonView) {
		CompoundButton screener = buttonView != null ? buttonView : findViewById(R.id.screener);
		if (screener == null) {
			return;
		}
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
			updateScreener(screener, View.GONE, false);
			return;
		}
		var roleManager = getSystemService(RoleManager.class);
		if (roleManager == null || !roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
			updateScreener(screener, View.GONE, false);
			return;
		}
		var isRoleHeld = roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING);
		updateScreener(screener, View.VISIBLE, isRoleHeld);
		if (buttonView != null && !isRoleHeld) {
			var intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING);
			tryStartActivityForResult(intent, 0, null);
		}
	}

	private void requestRequestedPermissions() {
		try {
			var packageInfo = getPackageInfo(PackageManager.GET_PERMISSIONS);
			var set = new HashSet<>(packageInfo.requestedPermissions != null
					? List.of(packageInfo.requestedPermissions)
					: List.of());
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
				set.remove(POST_NOTIFICATIONS);
			}
			if (!set.isEmpty()) {
				var permissions = set.toArray(new String[]{});
				requestPermissions(permissions, 0);
			}
		} catch (Throwable t) {
			Log.w(TAG, t);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		updateContent(View.NO_ID, null);
		requestRequestedPermissions();
		CallReceiver.notifyBlockedCall(this, null);
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateScreener(null);
	}
}
