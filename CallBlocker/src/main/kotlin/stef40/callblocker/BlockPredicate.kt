package stef40.callblocker

import android.content.SharedPreferences

class BlockPredicate(
	sharedPreferences: SharedPreferences,
	private val contactPredicate: (String) -> Boolean? = { null }
) : (String) -> Boolean {

	var isBlockNonContacts = sharedPreferences.getBoolean(KEY_BLOCK_NON_CONTACTS, false)

	var isExcludeContacts = sharedPreferences.getBoolean(KEY_EXCLUDE_CONTACTS, false)

	var regex = sharedPreferences.getString(KEY_REGEX, null) ?: ""
		set(value) {
			value.matches(value.toRegex())
			field = value
		}

	var isMatches = sharedPreferences.getString(KEY_MATCHES, null)?.toBooleanStrictOrNull()

	fun put(
		editor: SharedPreferences.Editor
	): SharedPreferences.Editor = editor.putBoolean(KEY_BLOCK_NON_CONTACTS, isBlockNonContacts)
		.putBoolean(KEY_EXCLUDE_CONTACTS, isExcludeContacts)
		.putString(KEY_REGEX, regex)
		.putString(KEY_MATCHES, isMatches?.toString())

	override fun invoke(phoneNumber: String): Boolean {
		val isContact by lazy { contactPredicate(phoneNumber) }
		if (isBlockNonContacts && isContact == false) return true
		if (isExcludeContacts && isContact == true) return false
		val isMatches = isMatches ?: return false
		return isMatches == phoneNumber.matches(regex.toRegex())
	}

	companion object {

		private const val KEY_BLOCK_NON_CONTACTS = "block_non_contacts"

		private const val KEY_EXCLUDE_CONTACTS = "exclude_contacts"

		private const val KEY_REGEX = "regex"

		private const val KEY_MATCHES = "matches"
	}
}
