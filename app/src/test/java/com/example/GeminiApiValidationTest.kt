package com.example

import com.example.data.api.GeminiCandidate
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiResponse
import com.example.data.model.ChatMessage
import com.example.data.model.UserProfile
import org.junit.Assert.*
import org.junit.Test

class GeminiApiValidationTest {

    @Test
    fun testResponseParsingWithArabicCharacters() {
        val arabicText = "أهلاً يا باشا! 🌟 إزيك يا بطل؟ جدولك جاهز ومتظبط."
        val response = GeminiResponse(
            candidates = listOf(
                GeminiCandidate(
                    content = GeminiContent(
                        role = "model",
                        parts = listOf(GeminiPart(text = arabicText))
                    )
                )
            )
        )

        val parsedText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        assertNotNull(parsedText)
        assertEquals(arabicText, parsedText)
        assertTrue(parsedText!!.contains("أهلاً يا باشا!"))
    }

    @Test
    fun testChatMessageOrderAndMapping() {
        val history = listOf(
            ChatMessage(id = 1, sender = "USER", message = "عايز أنظم يومي"),
            ChatMessage(id = 2, sender = "NOUR", message = "من عينيا يا بطل"),
            ChatMessage(id = 3, sender = "USER", message = "وعندي مادة تقيلة النهاردة")
        )

        assertEquals(3, history.size)
        assertEquals("USER", history.first().sender)
        assertEquals("USER", history.last().sender)
    }

    @Test
    fun testUserProfileDefaults() {
        val profile = UserProfile(
            name = "عمر",
            grade = "تالتة ثانوي",
            secondaryInterests = "برمجة وذكاء اصطناعي"
        )

        assertEquals("عمر", profile.name)
        assertEquals("تالتة ثانوي", profile.grade)
    }
}
