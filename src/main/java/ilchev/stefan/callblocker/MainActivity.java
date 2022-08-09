package ilchev.stefan.callblocker;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.function.Consumer;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";

	private void updateContent(Consumer<CallBlocker> consumer) {
		var sharedPreferences = getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE);
		var callBlocker = new CallBlocker(sharedPreferences);
		if (consumer != null) {
			try {
				consumer.accept(callBlocker);
				callBlocker.put(sharedPreferences.edit())
						.apply();
			} catch (Throwable t) {
				TextView errorView = findViewById(R.id.error);
				if (errorView != null) {
					errorView.setText(t.getLocalizedMessage());
				}
				return;
			}
		}
		var regex = callBlocker.getRegex();
		var isMatches = callBlocker.isMatches();
		EditText regexView = findViewById(R.id.regex);
		if (regexView != null && !regex.equals("" + regexView.getText())) {
			regexView.setText(regex);
		}
		RadioGroup blockView = findViewById(R.id.block);
		if (blockView != null) {
			blockView.check(isMatches == null ? R.id.block_none
					: isMatches ? R.id.block_matches
					: R.id.block_others);
		}
		TextView errorView = findViewById(R.id.error);
		if (errorView != null) {
			errorView.setText("");
		}
	}

	private void initContent() {
		EditText regexView = findViewById(R.id.regex);
		if (regexView != null) {
			regexView.addTextChangedListener(new TextWatcher() {

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}

				@Override
				public void afterTextChanged(Editable s) {
					var regex = "" + s;
					updateContent(callBlocker ->
							callBlocker.setRegex(regex));
				}
			});
		}
		RadioGroup blockView = findViewById(R.id.block);
		if (blockView != null) {
			blockView.setOnCheckedChangeListener((group, checkedId) -> {
				var isMatches = checkedId == R.id.block_matches ? Boolean.TRUE
						: checkedId == R.id.block_others ? Boolean.FALSE
						: null;
				updateContent(callBlocker ->
						callBlocker.setMatches(isMatches));
			});
		}
	}

	private void requestRequestedPermissions() {
		try {
			var packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
			if (packageInfo.requestedPermissions != null && packageInfo.requestedPermissions.length > 0) {
				requestPermissions(packageInfo.requestedPermissions, 0);
			}
		} catch (Throwable t) {
			Log.w(TAG, t);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		updateContent(null);
		initContent();
		requestRequestedPermissions();
	}
}
