package com.rabbitclient

/**
 * Parses Bedrock versions such as 1.21.90, 1.21.100, and year-based 26.0.
 * Unknown future versions are detected, not automatically declared supported.
 */
data class GameVersion(val parts: List<Int>) : Comparable<GameVersion> {
    override fun compareTo(other: GameVersion): Int {
        val count = maxOf(parts.size, other.parts.size)
        for (index in 0 until count) {
            val left = parts.getOrElse(index) { 0 }
            val right = other.parts.getOrElse(index) { 0 }
            if (left != right) return left.compareTo(right)
        }
        return 0
    }

    override fun toString(): String = parts.joinToString(".")

    companion object {
        private val VERSION_PATTERN = Regex("""^v?(\d+(?:\.\d+){1,3})(?:[-+].*)?$""", RegexOption.IGNORE_CASE)

        fun parse(raw: String?): GameVersion? {
            val match = VERSION_PATTERN.matchEntire(raw?.trim().orEmpty()) ?: return null
            val parts = match.groupValues[1].split('.').map { it.toIntOrNull() ?: return null }
            if (parts.any { it < 0 }) return null
            return GameVersion(parts)
        }

        val minimumRequested = GameVersion(listOf(1, 21, 90))

        fun targetRangeStatus(raw: String?): String {
            val version = parse(raw) ?: return "Unknown version"
            // Minecraft switched to year-based numbering in 2026; treat 26.x as
            // a new version family requiring explicit validation, not as 1.22.
            if (version.parts.firstOrNull() == 26) return "Detected — runtime not verified"
            return if (version >= minimumRequested) "Detected — runtime not verified"
            else "Below target version"
        }
    }
}
