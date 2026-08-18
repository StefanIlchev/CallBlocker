package stef40.about

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun AboutTheme(
	isDarkTheme: Boolean = isSystemInDarkTheme(),
	isDynamicColor: Boolean = true,
	content: @Composable () -> Unit
) {
	val context = LocalContext.current
	MaterialTheme(
		colorScheme = if (isDynamicColor && Build.VERSION_CODES.R < Build.VERSION.SDK_INT) {
			if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
		} else {
			if (isDarkTheme) darkColorScheme() else lightColorScheme()
		},
		content = content
	)
}
