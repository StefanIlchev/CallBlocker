package ilchev.stefan.callblocker

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Parcelable
import android.util.Log
import org.json.JSONObject
import java.io.File
import java.net.URI

class UpdateService : Service() {

	private var workHandler: Handler? = null

	@Volatile
	private var updateVersionName: String? = null

	private var updateInstallReceiver: BroadcastReceiver? = null

	private var updateInstallId = 0

	private var updateDownloadReceiver: BroadcastReceiver? = null

	private var updateDownloadId = 0L

	@Suppress("deprecation", "KotlinRedundantDiagnosticSuppress")
	private fun getPackageInfo(
		flags: Int
	) = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
		packageManager.getPackageInfo(packageName, flags)
	} else {
		packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
	}

	private fun tryStartActivity(intent: Intent, options: Bundle?) {
		try {
			startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), options)
		} catch (t: Throwable) {
			Log.w(TAG, t)
		}
	}

	@SuppressLint("UnspecifiedRegisterReceiverFlag")
	private fun registerExportedReceiver(
		receiver: BroadcastReceiver,
		filter: IntentFilter
	) = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
		registerReceiver(receiver, filter, null, mainHandler)
	} else {
		registerReceiver(receiver, filter, null, mainHandler, RECEIVER_EXPORTED)
	}

	private fun startForeground(stopIntent: PendingIntent) {
		val applicationInfo = applicationInfo
		val applicationIcon = applicationInfo.icon
		val applicationLabel = packageManager.getApplicationLabel(applicationInfo)
		val stopUpdate = getString(R.string.stop_update)
		val builder = Notification.Builder(this, BuildConfig.APPLICATION_ID)
			.setSmallIcon(applicationIcon)
			.setContentTitle(applicationLabel)
			.setContentText(stopUpdate)
			.setContentIntent(stopIntent)
		val manager = getSystemService(NotificationManager::class.java)
		if (manager != null) {
			val channel = NotificationChannel(
				BuildConfig.APPLICATION_ID,
				applicationLabel,
				NotificationManager.IMPORTANCE_MIN
			)
			manager.createNotificationChannel(channel)
		}
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
			builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_DEFERRED)
		}
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
			startForeground(NOTIFICATION_ID, builder.build())
		} else {
			startForeground(NOTIFICATION_ID, builder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
		}
	}

	private fun stopForeground() {
		stopForeground(STOP_FOREGROUND_REMOVE)
		stopSelf()
	}

	private fun stopWorkLooper() {
		workHandler?.apply {
			workHandler = null
			looper.quitSafely()
		}
	}

	private fun startWorkLooper() {
		stopWorkLooper()
		val workThread = HandlerThread(TAG)
		workThread.start()
		workHandler = Handler(workThread.looper)
	}

	private fun postUpdateStop(versionName: String?) {
		mainHandler.post {
			if (versionName == updateVersionName) {
				stopForeground()
			}
		}
	}

	private fun unregisterUpdateReceiver(
		versionName: String,
		receiver: BroadcastReceiver
	) = if (versionName == updateVersionName) {
		false
	} else {
		unregisterReceiver(receiver)
		true
	}

	private fun stopUpdateInstall() {
		updateInstallReceiver?.also {
			updateInstallReceiver = null
			unregisterReceiver(it)
		}
		updateInstallId.takeIf { it != 0 }?.also {
			updateInstallId = 0
			try {
				packageManager.packageInstaller.abandonSession(it)
			} catch (ignored: Throwable) {
			}
		}
	}

	private fun startUpdateInstall(file: File, versionName: String, workHandler: Handler) {
		if (!file.isFile) {
			stopForeground()
			return
		}
		val receiver = object : BroadcastReceiver() {

			override fun onReceive(context: Context?, intent: Intent?) {
				if (unregisterUpdateReceiver(versionName, this) || intent == null) return
				val id = intent.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, 0)
				if (id != 0 && id != updateInstallId || intent.action != BuildConfig.APPLICATION_ID) return
				updateInstallId = 0
				updateInstallReceiver = null
				unregisterReceiver(this)
				val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
				val activity = if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
					getParcelableExtra(intent, Intent.EXTRA_INTENT, Intent::class.java)
				} else {
					null
				}
				if (activity != null) {
					updateVersionName = null
					tryStartActivity(activity, null)
				} else if (status == PackageInstaller.STATUS_SUCCESS) {
					updateVersionName = null
				}
				stopForeground()
			}
		}
		val filter = IntentFilter(BuildConfig.APPLICATION_ID)
		registerExportedReceiver(receiver, filter)
		updateInstallReceiver = receiver
		val installer = packageManager.packageInstaller
		val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
		var flags = PendingIntent.FLAG_UPDATE_CURRENT
		params.setSize(file.length())
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
			params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
			flags = flags or if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
				PendingIntent.FLAG_IMMUTABLE
			} else {
				PendingIntent.FLAG_MUTABLE
			}
		}
		val updateInstallId = installer.createSession(params).also { updateInstallId = it }
		val statusReceiver = PendingIntent.getBroadcast(
			this,
			updateInstallId,
			Intent(BuildConfig.APPLICATION_ID),
			flags
		).intentSender
		workHandler.post {
			try {
				installer.openSession(updateInstallId).use { session ->
					session.openWrite(file.name, 0L, file.length()).use { out ->
						file.inputStream().use { it.copyTo(out) }
						session.fsync(out)
					}
					session.commit(statusReceiver)
				}
			} catch (t: Throwable) {
				Log.w(TAG, t)
				postUpdateStop(versionName)
			}
		}
	}

	private fun stopUpdateDownload(): Int {
		updateDownloadReceiver?.also {
			updateDownloadReceiver = null
			unregisterReceiver(it)
		}
		return updateDownloadId.takeIf { it != 0L }?.let {
			updateDownloadId = 0L
			val manager = getSystemService(DownloadManager::class.java)
			manager?.remove(updateDownloadId)
		} ?: 0
	}

	private fun startUpdateDownload(versionName: String, fileName: String, downloadUri: Uri, workHandler: Handler) {
		val manager = getSystemService(DownloadManager::class.java) ?: return stopForeground()
		val dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return stopForeground()
		val file = File(dir, fileName)
		val receiver = object : BroadcastReceiver() {

			override fun onReceive(context: Context?, intent: Intent?) {
				if (unregisterUpdateReceiver(versionName, this) || intent == null) return
				when (intent.action) {
					DownloadManager.ACTION_DOWNLOAD_COMPLETE -> {
						val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L)
						if (id != 0L && id == updateDownloadId) {
							updateDownloadId = 0L
							updateDownloadReceiver = null
							unregisterReceiver(this)
							try {
								startUpdateInstall(file, versionName, workHandler)
							} catch (t: Throwable) {
								Log.w(TAG, t)
								stopForeground()
							}
						}
					}

					DownloadManager.ACTION_NOTIFICATION_CLICKED -> {
						val id = updateDownloadId
						val ids = intent.getLongArrayExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS)
						if (id != 0L && ids?.contains(id) == true) {
							stopForeground()
						}
					}

					else -> Unit
				}
			}
		}
		val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
		filter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED)
		registerExportedReceiver(receiver, filter)
		updateDownloadReceiver = receiver
		val applicationInfo = applicationInfo
		val applicationLabel = packageManager.getApplicationLabel(applicationInfo)
		val stopUpdate = getString(R.string.stop_update)
		val request = DownloadManager.Request(downloadUri)
			.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, fileName)
			.setTitle(applicationLabel)
			.setDescription(stopUpdate)
		val updateDownloadId = manager.enqueue(request).also { updateDownloadId = it }
		val query = DownloadManager.Query().setFilterById(updateDownloadId)
		workHandler.post(object : Runnable {

			override fun run() {
				try {
					val status = manager.query(query).use {
						if (it?.moveToFirst() == true) {
							it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
						} else {
							DownloadManager.STATUS_FAILED
						}
					}
					if (status == DownloadManager.STATUS_SUCCESSFUL || versionName != updateVersionName) return
					if (status != DownloadManager.STATUS_FAILED) {
						workHandler.postDelayed(this, 1_000L)
						return
					}
				} catch (t: Throwable) {
					Log.w(TAG, t)
				}
				postUpdateStop(versionName)
			}
		})
	}

	private fun stopUpdate() {
		updateVersionName = null
		stopUpdateInstall()
		stopUpdateDownload()
		try {
			getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.deleteRecursively()
		} catch (t: Throwable) {
			Log.w(TAG, t)
		}
	}

	private fun startUpdate(versionName: String, fileName: String, downloadUri: Uri, workHandler: Handler) {
		stopUpdate()
		updateVersionName = versionName
		startUpdateDownload(versionName, fileName, downloadUri, workHandler)
	}

	override fun onCreate() {
		try {
			val stopIntent = PendingIntent.getActivity(
				this,
				NOTIFICATION_ID,
				Intent(this, MainActivity::class.java),
				PendingIntent.FLAG_IMMUTABLE
			)
			startForeground(stopIntent)
			startWorkLooper()
		} catch (t: Throwable) {
			Log.w(TAG, t)
		}
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		workHandler?.post {
			try {
				val latestRelease = JSONObject(URI.create(BuildConfig.LATEST_RELEASE_URL).toURL().readText())
				val latestVersion = latestRelease.getString(KEY_NAME)
				val fileName = "${BuildConfig.PROJECT_NAME}-${BuildConfig.BUILD_TYPE}-$latestVersion.apk"
				val assets = latestRelease.getJSONArray(KEY_ASSETS)
				for (i in 0 until assets.length()) {
					val asset = assets.getJSONObject(i)
					if (asset.getString(KEY_NAME) != fileName) continue
					val downloadUri = Uri.parse(asset.getString(KEY_DOWNLOAD_URI))
					mainHandler.post main@{
						val workHandler = workHandler ?: return@main
						try {
							val versionName = latestVersion.takeIf { getPackageInfo(0).versionName != it }
							if (versionName == null) {
								stopForeground()
							} else if (versionName != updateVersionName) {
								startUpdate(versionName, fileName, downloadUri, workHandler)
							}
						} catch (t: Throwable) {
							Log.w(TAG, t)
							stopForeground()
						}
					}
					return@post
				}
				Log.v(TAG, "\"$KEY_ASSETS\": $assets")
			} catch (t: Throwable) {
				Log.v(TAG, "${t.message}")
			}
			postUpdateStop(null)
		}
		return START_STICKY
	}

	override fun onDestroy() {
		try {
			stopWorkLooper()
			stopUpdate()
		} catch (t: Throwable) {
			Log.w(TAG, t)
		}
	}

	override fun onBind(intent: Intent?) = null

	companion object {

		private const val TAG = "UpdateService"

		const val NOTIFICATION_ID = Int.MAX_VALUE

		private const val KEY_NAME = "name"

		private const val KEY_ASSETS = "assets"

		private const val KEY_DOWNLOAD_URI = "browser_download_url"

		private val mainHandler = Handler(Looper.getMainLooper())

		@Suppress("deprecation")
		private fun <T : Parcelable?> getParcelableExtra(
			intent: Intent,
			name: String,
			clazz: Class<T>
		) = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
			intent.getParcelableExtra(name)
		} else {
			intent.getParcelableExtra(name, clazz)
		}
	}
}
