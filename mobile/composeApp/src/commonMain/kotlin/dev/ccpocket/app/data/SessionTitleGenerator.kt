package dev.ccpocket.app.data

/**
 * Produces a short, local session title without making a network/model request. Keeping this pure and
 * CPU-only lets [PocketRepository] run it on Dispatchers.Default, entirely outside the chat stream.
 */
internal fun generateSessionTitle(prompt: String): String? {
    var text = prompt
        .replace(Regex("```[\\s\\S]*?```"), " ")
        .replace(Regex("<[^>]+>"), " ")
        .replace(Regex("https?://\\S+"), " ")
        .replace(Regex("[`#*_>]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()

    if (text.isBlank()) return null

    // Drop conversational scaffolding so the title describes the task instead of copying its opening.
    text = text
        .replace(Regex("^(?:为了[^，,。]{1,30}[，,]\\s*)?(?:请(?:帮我)?|麻烦(?:帮我)?|我(?:想|希望|需要)|能否|可以(?:帮我)?|帮我)\\s*"), "")
        .replace(Regex("^(?:please\\s+)?(?:help\\s+me\\s+)?(?:i\\s+(?:want|need|would\\s+like)\\s+(?:to\\s+)?)", RegexOption.IGNORE_CASE), "")
        .trimStart(' ', '：', ':', '，', ',')

    val firstThought = text.split(Regex("[。！？!?\\n]"), limit = 2).first().trim()
    if (firstThought.isBlank()) return null

    val maxLength = if (firstThought.any { it.code in 0x3400..0x9FFF }) 28 else 56
    return firstThought.take(maxLength).trimEnd(' ', '，', ',', '。', '.', '：', ':', '；', ';', '-', '—')
        .takeIf { it.isNotBlank() }
}

internal fun shouldGenerateSessionTitle(
    title: String?,
    userMessageCount: Int,
    prompt: String,
    alreadyAttempted: Boolean,
): Boolean = title.isNullOrBlank() && userMessageCount == 1 && prompt.isNotBlank() && !alreadyAttempted
