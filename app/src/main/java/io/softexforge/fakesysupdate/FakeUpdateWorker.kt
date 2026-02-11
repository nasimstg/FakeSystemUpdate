package io.softexforge.fakesysupdate

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class FakeUpdateWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val style = inputData.getString("style") ?: "stock"
        val durationMinutes = inputData.getInt("duration_minutes", 30)
        val keepScreenOn = inputData.getBoolean("keep_screen_on", true)
        val exitMethod = inputData.getString("exit_method") ?: "triple_tap"

        ensureNotificationChannel()

        val intent = Intent(applicationContext, FakeUpdateActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra(SetupActivity.EXTRA_STYLE, style)
            putExtra(SetupActivity.EXTRA_DURATION_MINUTES, durationMinutes)
            putExtra(SetupActivity.EXTRA_KEEP_SCREEN_ON, keepScreenOn)
            putExtra(SetupActivity.EXTRA_EXIT_METHOD, exitMethod)
            putExtra(SetupActivity.EXTRA_APP_PINNING, false)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentTitle("System Update")
            .setContentText("Installing system update...")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        return Result.success()
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                applicationContext.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Triggers scheduled prank updates"
            }
            val manager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "fake_update_trigger"
        private const val NOTIFICATION_ID = 1001
    }
}
