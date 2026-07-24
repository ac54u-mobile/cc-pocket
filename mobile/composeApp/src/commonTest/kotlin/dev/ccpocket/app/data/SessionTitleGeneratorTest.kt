package dev.ccpocket.app.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SessionTitleGeneratorTest {
    @Test
    fun chineseRequestScaffoldingIsRemoved() {
        assertEquals("在后台异步完成标题的生成和 UI 更新", generateSessionTitle("为了保证用户体验，我希望在后台异步完成标题的生成和 UI 更新。"))
    }

    @Test
    fun onlyFirstMeaningfulSentenceIsUsed() {
        assertEquals("修复会话标题", generateSessionTitle("请帮我修复会话标题！然后运行测试。"))
    }

    @Test
    fun markupOnlyPromptDoesNotProduceTitle() {
        assertNull(generateSessionTitle("https://example.com ```code```"))
    }

    @Test
    fun triggerIsOneShotAndFirstUserMessageOnly() {
        assertTrue(shouldGenerateSessionTitle(null, 1, "hello", alreadyAttempted = false))
        assertFalse(shouldGenerateSessionTitle(null, 2, "again", alreadyAttempted = false))
        assertFalse(shouldGenerateSessionTitle("Existing", 1, "hello", alreadyAttempted = false))
        assertFalse(shouldGenerateSessionTitle(null, 1, "hello", alreadyAttempted = true))
    }
}
