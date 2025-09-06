@file:Suppress("DEPRECATION")

package stef40.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.aboutlibraries.LibsConfiguration
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.util.SpecialButton
import stef40.base.tryStartActivity
import stef40.base.tryStartForegroundService
import stef40.base.usableUpdateServiceType

private const val TAG = "AboutUtilities"

private val gitHubUri = try {
	BuildConfig.GIT_HUB_URL.ifEmpty { null }?.let(Uri::parse)
} catch (t: Throwable) {
	Log.w(TAG, t)
	null
}

private val sponsorUri = try {
	BuildConfig.SPONSOR_URL.ifEmpty { null }?.let(Uri::parse)
} catch (t: Throwable) {
	Log.w(TAG, t)
	null
}

private fun Context.onExtraClicked(
	specialButton: SpecialButton
) = when (specialButton) {
	SpecialButton.SPECIAL1 -> gitHubUri?.let { tryStartActivity(Intent(Intent.ACTION_VIEW, it)) }
	SpecialButton.SPECIAL2 -> sponsorUri?.let { tryStartActivity(Intent(Intent.ACTION_VIEW, it)) }
	SpecialButton.SPECIAL3 -> usableUpdateServiceType?.let { tryStartForegroundService(Intent(this, it)) }
} != null

private val libsListener = object : LibsConfiguration.LibsListener {

	override fun onIconClicked(v: View) = Unit

	override fun onLibraryAuthorClicked(v: View, library: Library) = false

	override fun onLibraryContentClicked(v: View, library: Library) = false

	override fun onLibraryBottomClicked(v: View, library: Library) = false

	override fun onExtraClicked(v: View, specialButton: SpecialButton) = v.context.onExtraClicked(specialButton)

	override fun onIconLongClicked(v: View) = false

	override fun onLibraryAuthorLongClicked(v: View, library: Library) = false

	override fun onLibraryContentLongClicked(v: View, library: Library) = false

	override fun onLibraryBottomLongClicked(v: View, library: Library) = false
}

fun Context.tryStartAboutActivity(appName: String) {
	try {
		LibsBuilder().apply {
			sort = true
			showLicense = true
			showVersion = true
			aboutShowIcon = true
			aboutAppName = appName
			aboutShowVersion = true
			aboutShowVersionName = true
			aboutShowVersionCode = true
			gitHubUri?.let { aboutAppSpecial1 = getString(R.string.git_hub) }
			sponsorUri?.let { aboutAppSpecial2 = getString(R.string.sponsor) }
			usableUpdateServiceType?.let { aboutAppSpecial3 = getString(R.string.update) }
			activityTitle = getString(R.string.about)
			searchEnabled = true
		}.start(this)
		LibsConfiguration.listener = libsListener
	} catch (t: Throwable) {
		Log.w(TAG, t)
	}
}
