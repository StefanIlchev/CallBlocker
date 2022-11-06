package ilchev.stefan.callblocker;

import android.content.SharedPreferences;

import java.util.function.Predicate;

public class CallPredicate implements Predicate<String> {

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

	public CallPredicate(SharedPreferences sharedPreferences) {
		setRegex(sharedPreferences.getString(KEY_REGEX, ""));
		setMatches(switch (sharedPreferences.getString(KEY_MATCHES, null + "")) {
			case true + "" -> Boolean.TRUE;
			case false + "" -> Boolean.FALSE;
			default -> null;
		});
	}

	public SharedPreferences.Editor put(SharedPreferences.Editor editor) {
		return editor.putString(KEY_REGEX, getRegex())
				.putString(KEY_MATCHES, isMatches() + "");
	}

	@Override
	public boolean test(String phoneNumber) {
		var isMatches = isMatches();
		return isMatches != null && isMatches == phoneNumber.matches(getRegex());
	}
}
