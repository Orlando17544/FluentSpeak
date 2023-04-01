package com.example.android.fluentspeak

import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.assertion.PositionAssertions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.android.fluentspeak.network.Message
import org.hamcrest.Matcher
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
        val content2 = "Fine thank you"
        val content3 = "Nice to meet you"

        val userMessage1 = Message(MESSAGE_ROLE.USER, content1)
        val assistantMessage1 = Message(MESSAGE_ROLE.ASSISTANT, content2)
        val userMessage2 = Message(MESSAGE_ROLE.USER, content3)

        var id1: Int? = null
        var id2: Int? = null
        var id3: Int? = null

        // When
        val scenario = launchFragmentInContainer<ConversationFragment>(themeResId = R.style.Theme_FluentSpeak)
        scenario.onFragment {
            id1 = it.addMessageToView(userMessage1)
            id2 = it.addMessageToView(assistantMessage1)
            id3 = it.addMessageToView(userMessage2)
        }

        // Then
        onView(withId(id1!!)).check(matches(withText(content1)))
        onView(withId(id2!!)).check(matches(withText(content2)))
        onView(withId(id3!!)).check(matches(withText(content3)))

        onView(withId(id1!!)).check(isCompletelyAbove(withId(id2!!)))
        onView(withId(id2!!)).check(isCompletelyAbove(withId(id3!!)))
        onView(withId(id1!!)).check(isCompletelyAbove(withId(id3!!)))

        onView(withId(id1!!)).check(isCompletelyRightOf(withId(id2!!)))
        onView(withId(id2!!)).check(isCompletelyLeftOf(withId(id3!!)))

        onView(withText("You are a helpful assistant.")).check(isCompletelyAbove(withId(id1!!)))
        onView(withText("You are a helpful assistant.")).check(isCompletelyAbove(withId(id2!!)))
        onView(withText("You are a helpful assistant.")).check(isCompletelyAbove(withId(id3!!)))
    }
}