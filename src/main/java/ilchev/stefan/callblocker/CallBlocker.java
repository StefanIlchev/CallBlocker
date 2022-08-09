package ilchev.stefan.callblocker;

import android.content.SharedPreferences;

public class CallBlocker {

	private static final String KEY_REGEX = "regex";

	private static final String KEY_MATCHES = "matches";

	private String regex;

	public String getRegex() {
		return regex;
	}

	public void setRegex(String value) {
		value.matches(value);
		regex = value;
	}

	private Boolean isMatches;

	public Boolean isMatches() {
		return isMatches;
	}

	public void setMatches(Boolean value) {
		isMatches = value;
	}

	public CallBlocker(SharedPreferences sharedPreferences) {
		setRegex(sharedPreferences.getString(KEY_REGEX, ""));
		switch (sharedPreferences.getString(KEY_MATCHES, null + "")) {
			case true + "":
				setMatches(Boolean.TRUE);
				break;
			case false + "":
				setMatches(Boolean.FALSE);
				break;
			default:
				setMatches(null);
		}
	}

	public SharedPreferences.Editor put(SharedPreferences.Editor editor) {
		return editor.putString(KEY_REGEX, getRegex())
				.putString(KEY_MATCHES, isMatches() + "");
	}

	public boolean isBlocked(String phoneNumber) {
		var regex = getRegex();
		var isMatches = isMatches();
		return isMatches != null && isMatches == phoneNumber.matches(regex);
	}
}
