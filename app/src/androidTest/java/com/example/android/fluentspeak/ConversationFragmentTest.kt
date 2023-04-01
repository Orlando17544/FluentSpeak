package com.example.android.fluentspeak

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.android.fluentspeak.network.Message
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4::class)
class ConversationFragmentTest {
    @Test
    fun addMessageToView_threeMessagesAdded_threeMessagesDisplayed() {
        // Given
        val content1 = "Hi, how are you?"
        val content2 = "Fine thank you, what is your name?"
        val content3 = "Alberto, nice to meet you"

        val userMessagePortion1 = Message(MESSAGE_ROLE.USER, content1)
        val userMessagePortion2 = Message(MESSAGE_ROLE.ASSISTANT, content2)
        val userMessagePortion3 = Message(MESSAGE_ROLE.USER, content3)

        var id1: Int? = null
        var id2: Int? = null
        var id3: Int? = null

        // When
        val scenario = launchFragmentInContainer<ConversationFragment>(themeResId = R.style.Theme_FluentSpeak)
        scenario.onFragment {
            id1 = it.addMessageToView(userMessagePortion1)
            id2 = it.addMessageToView(userMessagePortion2)
            id3 = it.addMessageToView(userMessagePortion3)
        }

        // Then
        onView(withId(id1!!)).check(matches(withText(content1)))
        onView(withId(id2!!)).check(matches(withText(content2)))
        onView(withId(id3!!)).check(matches(withText(content3)))
    }
}