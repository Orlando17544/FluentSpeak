package com.example.android.fluentspeak.test.conversation

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.fluentspeak.ConversationData
import com.example.android.fluentspeak.ConversationViewModel
import com.example.android.fluentspeak.MESSAGE_ROLE
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

        val conversationViewModel = ConversationViewModel(apisRepository)
        val userMessagePortion1 = Message(MESSAGE_ROLE.USER, content1)
        val userMessagePortion2 = Message(MESSAGE_ROLE.USER, content2)
        val userMessagePortion3 = Message(MESSAGE_ROLE.USER, content3)

        // When
        conversationViewModel.addMessageToUnfinishedUserMessage(userMessagePortion1)
        conversationViewModel.addMessageToUnfinishedUserMessage(userMessagePortion2)
        conversationViewModel.addMessageToUnfinishedUserMessage(userMessagePortion3)


        // Then
        assertEquals(content1 + " " + content2 + " " + content3, conversationViewModel.unfinishedUserMessage.content)
    }

    @Test
    fun addMessageToConversationData_addThreeMessages_returnsThreeMessages() {
        // Given
        val content1 = "You are a helpful assistant."
        val content2 = "Hi, how are you?"
        val content3 = "my name is Alberto"
        val content4 = "and I like to go to the gym"

        val conversationViewModel = ConversationViewModel(apisRepository)
        val systemMessage = Message(MESSAGE_ROLE.SYSTEM, content1)
        val userMessage1 = Message(MESSAGE_ROLE.USER, content2)
        val assistantMessage1 = Message(MESSAGE_ROLE.ASSISTANT, content3)
        val userMessage2 = Message(MESSAGE_ROLE.USER, content4)

        // When
        conversationViewModel.addMessageToConversationData(systemMessage)
        conversationViewModel.addMessageToConversationData(userMessage1)
        conversationViewModel.addMessageToConversationData(assistantMessage1)
        conversationViewModel.addMessageToConversationData(userMessage2)

        // Then
        assertEquals(4, ConversationData.messages.size)
    }

    @Test
    fun addMessageToConversationData_addEightMessages_returnsFiveMessages() {
        // Given
        val content1 = "You are a helpful assistant."
        val content2 = "Hi, how are you?"
        val content3 = "my name is Alberto"
        val content4 = "and I like to go to the gym"
        val content5 = "What do you like to do?"
        val content6 = "I sometimes listen to music on youtube"
        val content7 = "I also like to watch youtube videos"
        val content8 = "What is your favorite color?"

        val conversationViewModel = ConversationViewModel(apisRepository)
        val systemMessage = Message(MESSAGE_ROLE.SYSTEM, content1)
        val userMessage1 = Message(MESSAGE_ROLE.USER, content2)
        val assistantMessage1 = Message(MESSAGE_ROLE.ASSISTANT, content3)
        val userMessage2 = Message(MESSAGE_ROLE.USER, content4)
        val assistantMessage2 = Message(MESSAGE_ROLE.ASSISTANT, content5)
        val userMessage3 = Message(MESSAGE_ROLE.USER, content6)
        val assistantMessage3 = Message(MESSAGE_ROLE.ASSISTANT, content7)
        val userMessage4 = Message(MESSAGE_ROLE.USER, content8)

        // When
        conversationViewModel.addMessageToConversationData(systemMessage)
        conversationViewModel.addMessageToConversationData(userMessage1)
        conversationViewModel.addMessageToConversationData(assistantMessage1)
        conversationViewModel.addMessageToConversationData(userMessage2)
        conversationViewModel.addMessageToConversationData(assistantMessage2)
        conversationViewModel.addMessageToConversationData(userMessage3)
        conversationViewModel.addMessageToConversationData(assistantMessage3)
        conversationViewModel.addMessageToConversationData(userMessage4)

        // Then
        assertEquals(5, ConversationData.messages.size)
        assertEquals(Message(MESSAGE_ROLE.SYSTEM, content1), ConversationData.messages[0])
        assertEquals(Message(MESSAGE_ROLE.ASSISTANT, content5), ConversationData.messages[1])
        assertEquals(Message(MESSAGE_ROLE.USER, content8), ConversationData.messages[4])
    }
}