package ilchev.stefan.callblocker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.function.Function;

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

	@SuppressWarnings({"deprecation", "RedundantSuppression"})
	private PackageInfo getPackageInfo(int flags) throws PackageManager.NameNotFoundException {
		var packageManager = getPackageManager();
		var packageName = getPackageName();
		return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
				? packageManager.getPackageInfo(packageName, flags)
				: packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags));
	}

	private void updateContent(int sourceId, Function<CallBlocker, Boolean> function) {
		var sharedPreferences = getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE);
		String error = null;
		try {
			var callBlocker = new CallBlocker(sharedPreferences);
			var isLoop = function != null ? function.apply(callBlocker) : null;
			if (isLoop != null) {
				if (isLoop) {
					return;
				}
				callBlocker.put(sharedPreferences.edit()).apply();
			}
			EditText regexView = sourceId != R.id.regex ? findViewById(R.id.regex) : null;
			if (regexView != null) {
				regexView.setText(callBlocker.getRegex());
			}
			RadioGroup blockView = sourceId != R.id.block ? findViewById(R.id.block) : null;
			if (blockView != null) {
				blockView.check(toBlockId(callBlocker.isMatches()));
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
			updateContent(R.id.regex, callBlocker -> {
				var result = regex.equals(callBlocker.getRegex());
				callBlocker.setRegex(regex);
				return result;
			});
		}
	};

	private final RadioGroup.OnCheckedChangeListener blockListener = (group, checkedId) -> {
		var isMatches = toBoolean(checkedId);
		updateContent(R.id.block, callBlocker -> {
			var result = isMatches == callBlocker.isMatches();
			callBlocker.setMatches(isMatches);
			return result;
		});
	};

	private void initContent() {
		EditText regexView = findViewById(R.id.regex);
		if (regexView != null) {
			regexView.removeTextChangedListener(regexListener);
			regexView.addTextChangedListener(regexListener);
		}
		RadioGroup blockView = findViewById(R.id.block);
		if (blockView != null) {
			blockView.setOnCheckedChangeListener(blockListener);
		}
	}

	private void requestRequestedPermissions() {
		try {
			var packageInfo = getPackageInfo(PackageManager.GET_PERMISSIONS);
			var set = packageInfo.requestedPermissions != null
					? new HashSet<>(Arrays.asList(packageInfo.requestedPermissions))
					: Collections.<String>emptySet();
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
		initContent();
		requestRequestedPermissions();
	}
}
