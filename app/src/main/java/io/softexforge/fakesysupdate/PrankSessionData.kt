package io.softexforge.fakesysupdate

data class PrankSessionData(
    val victimName: String,
    val pranksterName: String,  // Collected in exit interview
    val reactionType: String,
    val prankRating: Int,
    val templateIndex: Int,
    val messageIndex: Int,
    val durationMs: Long,
    val tapCount: Int,
    val exitMethod: String,
    val updateStyle: String,
    val customMessage: String? = null
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

    // Observation log text for medical/diagnostic templates
    val observationLog: String
        get() = when (reactionType) {
            "panicked" -> "Subject exhibited elevated cortisol levels. Rapid phone tapping detected. Diagnosis: Critical Gullibility."
            "stared" -> "Subject remained motionless for $formattedDuration. No vital signs of skepticism detected."
            "called_support" -> "Subject initiated emergency protocol. Attempted contact with external tech support."
            "hit_phone" -> "Subject displayed physical aggression toward device. Rage threshold exceeded."
            else -> "Subject behavior anomalous. Further observation required."
        }

    // Achievement story text for trophy/gamer templates
    val achievementStory: String
        get() {
            val adjective = when {
                durationMs > 180000 -> "legendary"  // >3min
                durationMs > 60000 -> "epic"        // >1min
                else -> "rare"
            }
            return "Executed a $adjective system update prank. Target fell for the fake loading screen and waited $formattedDuration before discovering the truth."
        }

    // IQ drop percentage (satirical stat)
    val iqDrop: Int
        get() = ((durationMs / 1000 / 10) + (prankRating * 5)).coerceIn(5, 99).toInt()
}
