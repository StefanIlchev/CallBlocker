package stef40.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import stef40.base.tryStartActivity

private const val TAG = "AboutUtilities"

val gitHubUri = try {
	BuildConfig.GIT_HUB_URL.ifEmpty { null }?.let(Uri::parse)
} catch (t: Throwable) {
	Log.w(TAG, t)
	null
}

val sponsorUri = try {
	BuildConfig.SPONSOR_URL.ifEmpty { null }?.let(Uri::parse)
} catch (t: Throwable) {
	Log.w(TAG, t)
	null
}

fun Context.tryStartAboutActivity(
	appName: String
) = tryStartActivity(Intent(this, AboutActivity::class.java).apply {
	putExtra(AboutActivity.EXTRA_APP_NAME, appName)
})
