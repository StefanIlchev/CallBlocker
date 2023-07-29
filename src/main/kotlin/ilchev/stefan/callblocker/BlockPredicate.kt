package ilchev.stefan.callblocker

import android.content.SharedPreferences

class BlockPredicate(sharedPreferences: SharedPreferences) : (String) -> Boolean {

	var regex = sharedPreferences.getString(KEY_REGEX, null) ?: ""
		set(value) {
			value.matches(value.toRegex())
			field = value
		}

	var isMatches = sharedPreferences.getString(KEY_MATCHES, null)?.toBooleanStrictOrNull()

	fun put(
		editor: SharedPreferences.Editor
	): SharedPreferences.Editor = editor.putString(KEY_REGEX, regex)
		.putString(KEY_MATCHES, isMatches?.toString())

	override fun invoke(phoneNumber: String): Boolean {
		val isMatches = isMatches ?: return false
		return isMatches == phoneNumber.matches(regex.toRegex())
	}

	companion object {

		private const val KEY_REGEX = "regex"

		private const val KEY_MATCHES = "matches"
	}
}
