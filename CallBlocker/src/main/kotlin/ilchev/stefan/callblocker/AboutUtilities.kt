@file:Suppress("DEPRECATION")

package ilchev.stefan.callblocker

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.aboutlibraries.LibsConfiguration
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.util.SpecialButton

private const val TAG = "AboutUtilities"

private val gitHubUri = try {
	BuildConfig.GIT_HUB_URL.takeUnless(CharSequence::isEmpty)?.let(Uri::parse)
} catch (t: Throwable) {
	Log.w(TAG, t)
	null
}

private val sponsorUri = try {
	BuildConfig.SPONSOR_URL.takeUnless(CharSequence::isEmpty)?.let(Uri::parse)
} catch (t: Throwable) {
	Log.w(TAG, t)
	null
}

private fun Context.onExtraClicked(
	specialButton: SpecialButton
) = when (specialButton) {
	SpecialButton.SPECIAL1 -> gitHubUri?.also {
		tryStartActivity(Intent(Intent.ACTION_VIEW, it))
	} != null

	SpecialButton.SPECIAL2 -> sponsorUri?.also {
		tryStartActivity(Intent(Intent.ACTION_VIEW, it))
	} != null

	SpecialButton.SPECIAL3 -> if (packageManager.canRequestPackageInstalls()) {
		tryStartForegroundService(Intent(this, UpdateService::class.java))
		true
	} else {
		false
	}
}

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

fun Context.tryStartAboutActivity() {
	try {
		LibsBuilder().apply {
			sort = true
			showLicense = true
			showVersion = true
			aboutShowIcon = true
			aboutAppName = getString(R.string.app_name)
			aboutShowVersion = true
			aboutShowVersionName = true
			aboutShowVersionCode = true
			gitHubUri?.also { aboutAppSpecial1 = getString(R.string.git_hub) }
			sponsorUri?.also { aboutAppSpecial2 = getString(R.string.sponsor) }
			if (BuildConfig.LATEST_RELEASE_URL.isNotEmpty() && packageManager.canRequestPackageInstalls()) {
				aboutAppSpecial3 = getString(R.string.update)
			}
			activityTitle = getString(R.string.about)
			searchEnabled = true
		}.start(this)
		LibsConfiguration.listener = libsListener
	} catch (t: Throwable) {
		Log.w(TAG, t)
	}
}
