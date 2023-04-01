package com.example.android.fluentspeak


import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.fluentspeak.network.Message
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConversationViewModelTest {

    @Test
    fun addMessageToUnfinishedUserMessage_addThreeUserMessage_unfinishedUserMessageWithThreeUserMessages() {
        // Given
        val content1 = "Hi, how are you?"
        val content2 = "my name is Alberto"
        val content3 = "and I like to go to the gym"

        val conversationViewModel = ConversationViewModel()
        val userMessagePortion1 = Message(MESSAGE_ROLE.USER.value, content1)
        val userMessagePortion2 = Message(MESSAGE_ROLE.USER.value, content2)
        val userMessagePortion3 = Message(MESSAGE_ROLE.USER.value, content3)

        // When
        conversationViewModel.addMessageToUnfinishedUserMessage(userMessagePortion1)
        conversationViewModel.addMessageToUnfinishedUserMessage(userMessagePortion2)
        conversationViewModel.addMessageToUnfinishedUserMessage(userMessagePortion3)


            // Then
        assertEquals(content1 + " " + content2 + " " + content3, conversationViewModel.unfinishedUserMessage.content)
    }
}