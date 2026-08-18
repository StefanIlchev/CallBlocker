package stef40.about

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.util.withJson
import stef40.base.getPackageInfo
import stef40.base.tryStartActivity
import stef40.base.tryStartForegroundService
import stef40.base.usableUpdateServiceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(appName: String, onBack: () -> Unit) {
	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(stringResource(R.string.about)) },
				navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
			)
		}
	) { contentPadding ->
		val context = LocalContext.current
		val libs = remember { Libs.Builder().withJson(context, R.raw.aboutlibraries).build() }
		val appIcon = remember { context.packageManager.getApplicationIcon(context.packageName) }
		val appVersion = remember { context.getPackageInfo().run { "Version $versionName ($longVersionCode)" } }
		LibrariesContainer(
			libraries = libs,
			modifier = Modifier.fillMaxSize(),
			contentPadding = contentPadding,
			header = {
				item {
					Column(
						modifier = Modifier
							.fillMaxWidth()
							.padding(16.dp),
						horizontalAlignment = Alignment.CenterHorizontally
					) {
						Image(
							bitmap = appIcon.toBitmap().asImageBitmap(),
							contentDescription = null,
							modifier = Modifier.size(64.dp)
						)
						Text(
							text = appName,
							modifier = Modifier.padding(8.dp),
							style = MaterialTheme.typography.titleLarge
						)
						Text(
							text = appVersion,
							color = MaterialTheme.colorScheme.onSurfaceVariant,
							style = MaterialTheme.typography.titleSmall
						)
					}
				}
				item {
					Row(
						Modifier
							.fillMaxWidth()
							.padding(horizontal = 8.dp)
					) {
						gitHubUri?.let {
							Button(
								{ context.tryStartActivity(Intent(Intent.ACTION_VIEW, it)) },
								Modifier
									.padding(8.dp)
									.weight(1.0F)
							) { Text(stringResource(R.string.git_hub)) }
						}
						sponsorUri?.let {
							Button(
								{ context.tryStartActivity(Intent(Intent.ACTION_VIEW, it)) },
								Modifier
									.padding(8.dp)
									.weight(1.0F)
							) { Text(stringResource(R.string.sponsor)) }
						}
						context.usableUpdateServiceType?.let {
							Button(
								{ context.tryStartForegroundService(Intent(context, it)) },
								Modifier
									.padding(8.dp)
									.weight(1.0F)
							) { Text(stringResource(R.string.update)) }
						}
					}
				}
			}
		)
	}
}
