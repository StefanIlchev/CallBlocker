package ilchev.stefan.callblocker

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.TextView

class MainActivity : Activity() {

	private val blockNonContactsListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
		updateContent(R.id.block_non_contacts) { it.isBlockNonContacts = isChecked }
	}

	private val excludeContactsListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
		updateContent(R.id.exclude_contacts) { it.isExcludeContacts = isChecked }
	}

	private val regexListener = object : TextWatcher {

		override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

		override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

		override fun afterTextChanged(s: Editable) {
			val regex = "$s"
			updateContent(R.id.regex) { it.regex = regex }
		}
	}

	private val blockListener = RadioGroup.OnCheckedChangeListener { _, checkedId ->
		val isMatches = checkedId.toBoolean()
		updateContent(R.id.block) { it.isMatches = isMatches }
	}

	private val screenerListener = CompoundButton.OnCheckedChangeListener { buttonView, _ ->
		updateScreener(buttonView)
	}

	@Suppress("deprecation", "KotlinRedundantDiagnosticSuppress")
	private fun getPackageInfo(
		flags: Int
	) = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
		packageManager.getPackageInfo(packageName, flags)
	} else {
		packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
	}

	private fun tryStartActivityForResult(intent: Intent, requestCode: Int, options: Bundle?) {
		try {
			startActivityForResult(intent, requestCode, options)
		} catch (t: Throwable) {
			Log.w(TAG, t)
		}
	}

	private fun <T : View> findViewById(
		sourceId: Int,
		id: Int
	) = if (sourceId != id) findViewById<T>(id) else null

	private fun updateContent(sourceId: Int, consumer: ((BlockPredicate) -> Unit)?) {
		val sharedPreferences = getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE)
		var error: String? = null
		try {
			val blockPredicate = BlockPredicate(sharedPreferences)
			if (consumer != null) {
				consumer(blockPredicate)
				blockPredicate.put(sharedPreferences.edit()).apply()
			}
			val blockNonContactsView = findViewById<CompoundButton>(sourceId, R.id.block_non_contacts)
			blockNonContactsView?.setChecked(blockPredicate.isBlockNonContacts, blockNonContactsListener)
			val excludeContactsView = findViewById<CompoundButton>(sourceId, R.id.exclude_contacts)
			excludeContactsView?.setChecked(blockPredicate.isExcludeContacts, excludeContactsListener)
			val regexView = findViewById<TextView>(sourceId, R.id.regex)
			if (regexView != null) {
				regexView.removeTextChangedListener(regexListener)
				regexView.text = blockPredicate.regex
				regexView.addTextChangedListener(regexListener)
			}
			val blockView = findViewById<RadioGroup>(sourceId, R.id.block)
			if (blockView != null) {
				blockView.setOnCheckedChangeListener(null)
				blockView.check(blockPredicate.isMatches.toBlockId())
				blockView.setOnCheckedChangeListener(blockListener)
			}
		} catch (t: Throwable) {
			error = t.localizedMessage
		} finally {
			val errorView = findViewById<TextView>(R.id.error)
			if (errorView != null) {
				errorView.text = error
			}
		}
	}

	private fun updateScreener(screener: CompoundButton, visibility: Int, isChecked: Boolean) {
		screener.visibility = visibility
		screener.setChecked(isChecked, screenerListener)
	}

	private fun updateScreener(buttonView: CompoundButton?) {
		val screener = buttonView ?: findViewById(R.id.screener) ?: return
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
			updateScreener(screener, View.GONE, false)
			return
		}
		val roleManager = getSystemService(RoleManager::class.java)
		if (roleManager?.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) != true) {
			updateScreener(screener, View.GONE, false)
			return
		}
		val isRoleHeld = roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
		updateScreener(screener, View.VISIBLE, isRoleHeld)
		if (buttonView != null && !isRoleHeld) {
			val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
			tryStartActivityForResult(intent, 0, null)
		}
	}

	private fun requestRequestedPermissions() {
		try {
			val packageInfo = getPackageInfo(PackageManager.GET_PERMISSIONS)
			val set = mutableSetOf(*packageInfo.requestedPermissions ?: emptyArray())
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
				set.remove(POST_NOTIFICATIONS)
			}
			if (set.isNotEmpty()) {
				val permissions = set.toTypedArray()
				requestPermissions(permissions, 0)
			}
		} catch (t: Throwable) {
			Log.w(TAG, t)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		updateContent(View.NO_ID, null)
		requestRequestedPermissions()
		CallReceiver.notifyBlockedCall(this, null)
	}

	override fun onResume() {
		super.onResume()
		updateScreener(null)
	}

	companion object {

		private const val TAG = "MainActivity"

		@SuppressLint("InlinedApi")
		private const val POST_NOTIFICATIONS = Manifest.permission.POST_NOTIFICATIONS

		private fun Boolean?.toBlockId() = when (this) {
			null -> R.id.block_none
			true -> R.id.block_matches
			else -> R.id.block_others
		}

		private fun Int.toBoolean() = when (this) {
			R.id.block_matches -> true
			R.id.block_others -> false
			else -> null
		}

		private fun CompoundButton.setChecked(value: Boolean, listener: CompoundButton.OnCheckedChangeListener) {
			setOnCheckedChangeListener(null)
			isChecked = value
			setOnCheckedChangeListener(listener)
		}
	}
}
