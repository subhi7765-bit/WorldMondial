package sa.mondial.world

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.work.Configuration as WorkConfiguration
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.hilt.work.HiltWorkerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import sa.mondial.world.core.data.LocalizationManager
import sa.mondial.world.core.sync.MatchSyncWorker
import java.io.File
import java.util.concurrent.TimeUnit
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

@HiltAndroidApp
class WorldMondialApplication : Application(), WorkConfiguration.Provider {

    @Inject
    lateinit var localizationManager: LocalizationManager

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: WorkConfiguration
        get() = WorkConfiguration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        // STEP 1: Enforce global Uncaught Exception Handler BEFORE super.onCreate to intercept Hilt/Firebase startup crashes
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val stackTrace = android.util.Log.getStackTraceString(throwable)
            val logText = "Thread: ${thread.name}\n\n🚨 CRASH LOG REPORT 🚨\n\n$stackTrace"
            
            try {
                // Save the error log directly into the app's externally accessible safe shared storage folder
                val file = File(getExternalFilesDir(null), "crash_log.txt")
                file.writeText(logText)
            } catch (e: Exception) {
                try {
                    // Fallback to secondary internal files directory if partition is locked
                    val file = File(filesDir, "crash_log.txt")
                    file.writeText(logText)
                } catch (inner: Exception) {
                    // Fail silently to prevent complete platform freezing
                }
            }
            
            // Kill the crashing process cleanly to avoid frozen system white screens
            android.os.Process.killProcess(android.os.Process.myPid())
            java.lang.System.exit(10)
        }

        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        createGlobalNotificationChannel()
        enqueueBackgroundSync()

        CoroutineScope(Dispatchers.IO).launch {
            val deviceLocale = Locale.getDefault().language
            val defaultLang = if (deviceLocale == "ar" || deviceLocale == "en") deviceLocale else "en"
            val savedLanguage = try {
                localizationManager.currentLanguage.firstOrNull() ?: defaultLang
            } catch (e: Exception) {
                defaultLang
            }
            launch(Dispatchers.Main) {
                val currentLanguageTags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
                if (currentLanguageTags != savedLanguage) {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(savedLanguage))
                    Timber.i("WorldMondialApplication: Asynchronously set application locale to $savedLanguage")
                }
            }
        }
    }

    private fun enqueueBackgroundSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = PeriodicWorkRequestBuilder<MatchSyncWorker>(
            6, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "MondialMatchSyncWork",
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )
        Timber.i("WorldMondialApplication: Enqueued unique periodic MatchSyncWorker every 6 hours.")
    }

    private fun createGlobalNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Match Alerts"
            val descriptionText = "Get live updates, reminders, and match score alerts"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("match_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Timber.i("WorldMondialApplication: Global match_channel initialized with high urgency.")
        }
    }
}
