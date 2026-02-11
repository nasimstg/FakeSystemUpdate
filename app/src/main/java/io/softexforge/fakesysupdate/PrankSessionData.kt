package io.softexforge.fakesysupdate

data class PrankSessionData(
    val victimName: String,
    val reactionType: String,
    val prankRating: Int,
    val templateIndex: Int,
    val messageIndex: Int,
    val durationMs: Long,
    val tapCount: Int,
    val exitMethod: String,
    val updateStyle: String
) {
    val formattedDuration: String
        get() {
            val totalSeconds = (durationMs / 1000).toInt()
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "${minutes}m ${seconds}s"
        }

    val formattedDurationClock: String
        get() {
            val totalSeconds = (durationMs / 1000).toInt()
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

    val panicLevel: Int
        get() {
            val base = (durationMs / 1000 / 10).toInt()
            val tapBonus = tapCount * 5
            val reactionBonus = when (reactionType) {
                "panicked" -> 30
                "called_support" -> 25
                "hit_phone" -> 20
                "stared" -> 10
                else -> 15
            }
            return (base + tapBonus + reactionBonus).coerceIn(0, 99)
        }

    val productivityLeak: String
        get() {
            val leak = (panicLevel * 0.95 + (tapCount % 10) * 0.1).coerceIn(0.0, 99.9)
            return String.format("%.1f%%", leak)
        }

    val cpuCyclesBurned: String
        get() {
            val cycles = durationMs / 1000.0 * 15.7 / 1000.0
            return String.format("%.1fM", cycles)
        }
}
