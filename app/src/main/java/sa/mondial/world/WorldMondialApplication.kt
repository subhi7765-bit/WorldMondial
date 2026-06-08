package sa.mondial.world

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
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
        // [صائد الأخطاء التشغيلية] - يوضع كأول سطر لحفظ كراش الواجهات أو الـ Hilt في ملف نصي فوراً
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val stackTrace = android.util.Log.getStackTraceString(throwable)
            val logText = "🚨 CRASH REPORT 🚨\nThread: ${thread.name}\n\n$stackTrace"
            try {
                val file = File(getExternalFilesDir(null), "crash_log.txt")
                file.writeText(logText)
            } catch (e: Exception) {
                // حماية صامتة لمنع تجميد النظام
            }
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
        }
    }
}
