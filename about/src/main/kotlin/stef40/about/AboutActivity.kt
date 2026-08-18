package stef40.about

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class AboutActivity : ComponentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: ""
		enableEdgeToEdge()
		setContent {
			AboutTheme {
				AboutScreen(appName) {
					finish()
				}
			}
		}
	}

	companion object {

		const val EXTRA_APP_NAME = "extra_app_name"
	}
}
