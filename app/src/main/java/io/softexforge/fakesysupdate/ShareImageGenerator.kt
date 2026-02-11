package io.softexforge.fakesysupdate

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import java.io.File
import java.io.FileOutputStream

class ShareImageGenerator(private val context: Context) {

    companion object {
        private const val IMAGE_WIDTH = 1080
    }

    private val templateLayouts = intArrayOf(
        R.layout.template_share_stats_base,
        R.layout.template_share_stats_variant,
        R.layout.template_share_crash_base,
        R.layout.template_share_crash_variant,
        R.layout.template_share_trophy_base,
        R.layout.template_share_trophy_variant
    )

    fun generate(data: PrankSessionData): Bitmap {
        val layoutRes = templateLayouts[data.templateIndex.coerceIn(0, templateLayouts.size - 1)]
        val view = LayoutInflater.from(context).inflate(layoutRes, null)

        populateTemplate(view, data)

        val widthSpec = View.MeasureSpec.makeMeasureSpec(IMAGE_WIDTH, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(widthSpec, heightSpec)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        val bitmap = Bitmap.createBitmap(
            view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        return bitmap
    }

    private fun populateTemplate(view: View, data: PrankSessionData) {
        when (data.templateIndex) {
            0 -> populateStatsBase(view, data)
            1 -> populateStatsVariant(view, data)
            2 -> populateCrashBase(view, data)
            3 -> populateCrashVariant(view, data)
            4 -> populateTrophyBase(view, data)
            5 -> populateTrophyVariant(view, data)
        }
    }

    private fun populateStatsBase(view: View, data: PrankSessionData) {
        val totalSeconds = (data.durationMs / 1000).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        view.findViewById<TextView>(R.id.text_time_value).text = "${minutes}m\n${seconds}s"
        view.findViewById<TextView>(R.id.text_productivity_leak).text = data.productivityLeak
        view.findViewById<TextView>(R.id.text_cpu_cycles).text = data.cpuCyclesBurned
    }

    private fun populateStatsVariant(view: View, data: PrankSessionData) {
        view.findViewById<TextView>(R.id.text_time_value).text = data.formattedDurationClock
        view.findViewById<TextView>(R.id.text_target_name).text = data.victimName.ifEmpty { "Target" }
        view.findViewById<TextView>(R.id.text_panic_percent).text = "${data.panicLevel}%"

        val gaugeView = view.findViewById<ImageView>(R.id.image_panic_gauge)
        gaugeView.setImageDrawable(
            PanicGaugeDrawable(
                level = data.panicLevel,
                trackColor = Color.parseColor("#1A2E23"),
                fillColor = Color.parseColor("#0DF259"),
                glowColor = Color.parseColor("#400DF259")
            )
        )

        val reactionText = getReactionLogText(data.reactionType)
        view.findViewById<TextView>(R.id.text_reaction_log).text = reactionText
    }

    private fun populateCrashBase(view: View, data: PrankSessionData) {
        // Static layout — no dynamic fields beyond the grid which is all static strings
    }

    private fun populateCrashVariant(view: View, data: PrankSessionData) {
        val name = data.victimName.ifEmpty { "GullibleFriend" }
        view.findViewById<TextView>(R.id.text_victim_tag).text = "User @$name"
    }

    private fun populateTrophyBase(view: View, data: PrankSessionData) {
        val amount = (data.durationMs / 1000 * 47619).toLong()
        view.findViewById<TextView>(R.id.text_simulated_amount).text =
            "$${String.format("%,d", amount)}.00"

        view.findViewById<TextView>(R.id.text_receipt_hash).text =
            "RECEIPT #${(data.durationMs % 99999).toInt().toString().padStart(5, '0')}\n" +
                    "TX_000_CYBER_PUNK_ACHIEVED_CONFUSION"
    }

    private fun populateTrophyVariant(view: View, data: PrankSessionData) {
        val desc = context.getString(R.string.share_trophy_desc, data.formattedDuration)
        view.findViewById<TextView>(R.id.text_achievement_desc).text = desc

        val ragePercent = (100 - data.panicLevel).coerceAtLeast(0)
        view.findViewById<TextView>(R.id.text_rage_level).text = "${ragePercent}% Detected"

        val xp = (data.durationMs / 1000 * 10).coerceIn(100, 9999)
        view.findViewById<TextView>(R.id.text_xp_gained).text = "+${xp} XP"
    }

    private fun getReactionLogText(reactionType: String): String {
        return when (reactionType) {
            "panicked" -> context.getString(R.string.reaction_panicked)
            "stared" -> context.getString(R.string.reaction_stared)
            "called_support" -> context.getString(R.string.reaction_called_support)
            "hit_phone" -> context.getString(R.string.reaction_hit_phone)
            else -> context.getString(R.string.reaction_panicked)
        }
    }

    fun saveBitmapToCache(bitmap: Bitmap): File {
        val dir = File(context.cacheDir, "shared_images")
        if (!dir.exists()) dir.mkdirs()

        // Clean up old files (older than 24h)
        dir.listFiles()?.forEach { file ->
            if (System.currentTimeMillis() - file.lastModified() > 24 * 60 * 60 * 1000) {
                file.delete()
            }
        }

        val file = File(dir, "prank_receipt_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }
}
