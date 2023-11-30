package ilchev.stefan.callblocker

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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

	private fun isActivityFound(
		intent: Intent
	) = try {
		val activityInfo = intent.resolveActivityInfo(packageManager, 0)
		activityInfo?.isEnabled == true && activityInfo.exported
	} catch (t: Throwable) {
		Log.w(TAG, t)
		false
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
			findViewById<CompoundButton>(sourceId, R.id.block_non_contacts)?.apply {
				setChecked(blockPredicate.isBlockNonContacts, blockNonContactsListener)
			}
			findViewById<CompoundButton>(sourceId, R.id.exclude_contacts)?.apply {
				setChecked(blockPredicate.isExcludeContacts, excludeContactsListener)
			}
			findViewById<TextView>(sourceId, R.id.regex)?.apply {
				removeTextChangedListener(regexListener)
				text = blockPredicate.regex
				addTextChangedListener(regexListener)
			}
			findViewById<RadioGroup>(sourceId, R.id.block)?.apply {
				setOnCheckedChangeListener(null)
				check(blockPredicate.isMatches.toBlockId())
				setOnCheckedChangeListener(blockListener)
			}
		} catch (t: Throwable) {
			error = t.localizedMessage
		} finally {
			findViewById<TextView>(R.id.error)?.text = error
		}
	}

	private fun updateScreener(buttonView: CompoundButton?, screenerVisibility: Int, isScreener: Boolean) {
		(buttonView ?: findViewById(R.id.screener))?.apply {
			visibility = screenerVisibility
			setChecked(isScreener, screenerListener)
		}
	}

	private fun updateScreener(buttonView: CompoundButton?) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
			updateScreener(buttonView, View.GONE, false)
			return
		}
		val roleManager = getSystemService(RoleManager::class.java)
		if (roleManager?.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) != true) {
			updateScreener(buttonView, View.GONE, false)
			return
		}
		val isScreener = roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
		updateScreener(buttonView, View.VISIBLE, isScreener)
		buttonView ?: return
		val intent = if (isScreener) {
			Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
		} else {
			roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
		}
		if (isActivityFound(intent)) {
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
		notifyBlockedCall(null)
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
