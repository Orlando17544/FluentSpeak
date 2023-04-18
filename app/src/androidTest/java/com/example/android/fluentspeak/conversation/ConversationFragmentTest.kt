package com.example.android.fluentspeak.conversation


import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.doubleClick
import androidx.test.espresso.assertion.PositionAssertions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.android.fluentspeak.*
import com.example.android.fluentspeak.network.ApisRepository
import com.example.android.fluentspeak.network.Message
import org.hamcrest.Matchers.allOf
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4::class)
class ConversationFragmentTest {

    private lateinit var repository: ApisRepository

    private val MILLISECONDS = 4000L

    @Before
    fun initRepository() {
        repository = ApisRepository(FakeOpenAIApi(), FakeGoogleCloudApi())
        ServiceLocator.apisRepository = repository
    }

    @Test
    fun addMessageToView_threeMessagesAdded_threeMessagesDisplayed() {
        // Given
        val content1 = "Hi, how are you?"
        val content2 = "Fine thank you"
        val content3 = "Nice to meet you"

        val userMessage1 = Message(MESSAGE_ROLE.USER.toString().lowercase(), content1)
        val assistantMessage1 = Message(MESSAGE_ROLE.ASSISTANT.toString().lowercase(), content2)
        val userMessage2 = Message(MESSAGE_ROLE.USER.toString().lowercase(), content3)

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

    @Test
    fun setupListeners_oneClickRecordButton_recordButtonEnabledStopButtonEnabledTranslateButtonDisabled() {
        // Given
        launchFragmentInContainer<ConversationFragment>(themeResId = R.style.Theme_FluentSpeak)

        // When
        onView(withId(R.id.record_button)).perform(click())

        // Then
        onView(withId(R.id.record_button)).check(matches(isEnabled()))
        onView(withId(R.id.stop_button)).check(matches(isEnabled()))
        onView(withId(R.id.translate_button)).check(matches(isNotEnabled()))
    }

    @Test
    fun setupListeners_twoClicksRecordButton_recordButtonEnabledStopButtonEnabledTranslateButtonEnabled() {
        // Given
        launchFragmentInContainer<ConversationFragment>(themeResId = R.style.Theme_FluentSpeak)

        // When
        onView(withId(R.id.record_button)).perform(click(), click())

        // Then
        onView(withId(R.id.record_button)).check(matches(isEnabled()))
        onView(withId(R.id.stop_button)).check(matches(isEnabled()))
        onView(withId(R.id.translate_button)).check(matches(isEnabled()))

        onView(withText("whisper")).check(isCompletelyBelow(withText("You are a helpful assistant.")))
    }

    @Test
    fun setupListeners_oneClickTranslateButton_recordButtonDisabledStopButtonDisabledTranslateButtonEnabled() {
        // Given
        launchFragmentInContainer<ConversationFragment>(themeResId = R.style.Theme_FluentSpeak)

        // When
        onView(withId(R.id.translate_button)).perform(click())

        // Then
        onView(withId(R.id.record_button)).check(matches(isNotEnabled()))
        onView(withId(R.id.stop_button)).check(matches(isNotEnabled()))
        onView(withId(R.id.translate_button)).check(matches(isEnabled()))
    }

    @Test
    fun setupListeners_twoClicksTranslateButton_recordButtonEnabledStopButtonDisabledTranslateButtonEnabled() {
        // Given
        launchFragmentInContainer<ConversationFragment>(themeResId = R.style.Theme_FluentSpeak)

        // When
        onView(withId(R.id.translate_button)).perform(click())
        Thread.sleep(MILLISECONDS)
        onView(withId(R.id.translate_button)).perform(click())
        Thread.sleep(MILLISECONDS)

        // Then
        onView(withId(R.id.record_button)).check(matches(isEnabled()))
        onView(withId(R.id.stop_button)).check(matches(isNotEnabled()))
        onView(withId(R.id.translate_button)).check(matches(isEnabled()))
    }

    @Test
    fun setupListeners_oneClickRecordButtonOneClickStopButton_recordButtonEnabledStopButtonDisabledTranslateButtonEnabled() {
        // Given
        launchFragmentInContainer<ConversationFragment>(themeResId = R.style.Theme_FluentSpeak)

        // When
        onView(withId(R.id.record_button)).perform(click())
        Thread.sleep(MILLISECONDS)
        onView(withId(R.id.stop_button)).perform(click())
        Thread.sleep(MILLISECONDS)

        // Then
        onView(withId(R.id.record_button)).check(matches(isEnabled()))
        onView(withId(R.id.stop_button)).check(matches(isNotEnabled()))
        onView(withId(R.id.translate_button)).check(matches(isEnabled()))

        onView(withText("whisper")).check(isCompletelyBelow(withText("You are a helpful assistant.")))
        onView(withText("chatgpt")).check(isCompletelyBelow(withText("whisper")))
        onView(withText("chatgpt")).check(isCompletelyLeftOf(withText("whisper")))
    }

    @Test
    fun setupListeners_twoClicksRecordButtonOneClickTranslateButton_recordButtonDisabledStopButtonDisabledTranslateButtonEnabled() {
        // Given
        launchFragmentInContainer<ConversationFragment>(themeResId = R.style.Theme_FluentSpeak)

        // When
        onView(withId(R.id.record_button)).perform(click(), click())
        Thread.sleep(MILLISECONDS)
        onView(withId(R.id.translate_button)).perform(click())
        Thread.sleep(MILLISECONDS)

        // Then
        onView(withId(R.id.record_button)).check(matches(isNotEnabled()))
        onView(withId(R.id.stop_button)).check(matches(isNotEnabled()))
        onView(withId(R.id.translate_button)).check(matches(isEnabled()))

        onView(withText("whisper")).check(isCompletelyBelow(withText("You are a helpful assistant.")))
    }

    @Test
    fun setupListeners_twoClicksRecordButtonTwoClicksTranslateButton_recordButtonEnabledStopButtonEnabledTranslateButtonEnabled() {
        // Given
        launchFragmentInContainer<ConversationFragment>(themeResId = R.style.Theme_FluentSpeak)

        // When
        onView(withId(R.id.record_button)).perform(click(), click())
        Thread.sleep(MILLISECONDS)
        onView(withId(R.id.translate_button)).perform(click(), click())
        Thread.sleep(MILLISECONDS)

        // Then
        onView(withId(R.id.record_button)).check(matches(isEnabled()))
        onView(withId(R.id.stop_button)).check(matches(isEnabled()))
        onView(withId(R.id.translate_button)).check(matches(isEnabled()))

        onView(withText("whisper")).check(isCompletelyBelow(withText("You are a helpful assistant.")))
    }

    @Test
    fun setupListeners_twoClicksRecordButtonTwoClicksTranslateButtonOneClickStopButton_recordButtonEnabledStopButtonDisabledTranslateButtonEnabled() {
        // Given
        launchFragmentInContainer<ConversationFragment>(themeResId = R.style.Theme_FluentSpeak)

        // When
        onView(withId(R.id.record_button)).perform(click(), click())
        Thread.sleep(MILLISECONDS)
        onView(withId(R.id.translate_button)).perform(click(), click())
        Thread.sleep(MILLISECONDS)
        onView(withId(R.id.stop_button)).perform(click())
        Thread.sleep(MILLISECONDS)

        // Then
        onView(withId(R.id.record_button)).check(matches(isEnabled()))
        onView(withId(R.id.stop_button)).check(matches(isNotEnabled()))
        onView(withId(R.id.translate_button)).check(matches(isEnabled()))

        onView(withText("whisper")).check(isCompletelyBelow(withText("You are a helpful assistant.")))
        onView(withText("chatgpt")).check(isCompletelyBelow(withText("whisper")))
        onView(withText("chatgpt")).check(isCompletelyLeftOf(withText("whisper")))
    }

    @Test
    fun setupListeners_twoClicksRecordButtonOneClickStopButton_recordButtonEnabledStopButtonDisabledTranslateButtonEnabled() {
        // Given
        launchFragmentInContainer<ConversationFragment>(themeResId = R.style.Theme_FluentSpeak)

        // When
        onView(withId(R.id.record_button)).perform(click(), click())
        Thread.sleep(MILLISECONDS)
        onView(withId(R.id.stop_button)).perform(click())
        Thread.sleep(MILLISECONDS)

        // Then
        onView(withId(R.id.record_button)).check(matches(isEnabled()))
        onView(withId(R.id.stop_button)).check(matches(isNotEnabled()))
        onView(withId(R.id.translate_button)).check(matches(isEnabled()))

        onView(withText("whisper")).check(isCompletelyBelow(withText("You are a helpful assistant.")))
        onView(withText("chatgpt")).check(isCompletelyBelow(withText("whisper")))
        onView(withText("chatgpt")).check(isCompletelyLeftOf(withText("whisper")))
    }

    @Test
    fun setupListeners_twoClicksRecordButtonOneClickStopButtonOneClickRecordButton_recordButtonEnabledStopButtonEnabledTranslateButtonDisabled() {
        // Given
        launchFragmentInContainer<ConversationFragment>(themeResId = R.style.Theme_FluentSpeak)

        // When
        onView(withId(R.id.record_button)).perform(click())
        Thread.sleep(MILLISECONDS)
        onView(withId(R.id.record_button)).perform(click())
        Thread.sleep(MILLISECONDS)
        onView(withId(R.id.stop_button)).perform(click())
        Thread.sleep(MILLISECONDS)
        onView(withId(R.id.record_button)).perform(click())
        Thread.sleep(MILLISECONDS)

        // Then
        onView(withId(R.id.record_button)).check(matches(isEnabled()))
        onView(withId(R.id.stop_button)).check(matches(isEnabled()))
        onView(withId(R.id.translate_button)).check(matches(isNotEnabled()))

        onView(withText("whisper")).check(isCompletelyBelow(withText("You are a helpful assistant.")))
        onView(withText("chatgpt")).check(isCompletelyBelow(withText("whisper")))
        onView(withText("chatgpt")).check(isCompletelyLeftOf(withText("whisper")))
    }

    @Test
    fun setupListeners_oneClickRecordButtonOneClickStopButtonOneClickRecordButton_recordButtonEnabledStopButtonEnabledTranslateButtonDisabled() {
        // Given
        launchFragmentInContainer<ConversationFragment>(themeResId = R.style.Theme_FluentSpeak)

        // When
        onView(withId(R.id.record_button)).perform(click())
        Thread.sleep(MILLISECONDS)
        onView(withId(R.id.stop_button)).perform(click())
        Thread.sleep(MILLISECONDS)
        onView(withId(R.id.record_button)).perform(click())
        Thread.sleep(MILLISECONDS)

        // Then
        onView(withId(R.id.record_button)).check(matches(isEnabled()))
        onView(withId(R.id.stop_button)).check(matches(isEnabled()))
        onView(withId(R.id.translate_button)).check(matches(isNotEnabled()))

        onView(withText("whisper")).check(isCompletelyBelow(withText("You are a helpful assistant.")))
        onView(withText("chatgpt")).check(isCompletelyBelow(withText("whisper")))
        onView(withText("chatgpt")).check(isCompletelyLeftOf(withText("whisper")))
    }

    @Test
    fun setupListeners_oneClickRecordButtonOneClickStopButtonOneClickRecordButtonOneClickStopButton_recordButtonEnabledStopButtonDisabledTranslateButtonEnabled() {
        // Given
        launchFragmentInContainer<ConversationFragment>(themeResId = R.style.Theme_FluentSpeak)

        // When
        onView(withId(R.id.record_button)).perform(click())
        Thread.sleep(MILLISECONDS)
        onView(withId(R.id.stop_button)).perform(click())
        Thread.sleep(MILLISECONDS)
        onView(withId(R.id.record_button)).perform(click())
        Thread.sleep(MILLISECONDS)
        onView(withId(R.id.stop_button)).perform(click())
        Thread.sleep(MILLISECONDS)

        // Then
        onView(withId(R.id.record_button)).check(matches(isEnabled()))
        onView(withId(R.id.stop_button)).check(matches(isNotEnabled()))
        onView(withId(R.id.translate_button)).check(matches(isEnabled()))

        // whisper
        onView(allOf(withParent(withId(R.id.chat_layout)), withParentIndex(1))).check(isCompletelyBelow(withText("You are a helpful assistant.")))

        // whisper and chatgpt
        onView(allOf(withParent(withId(R.id.chat_layout)), withParentIndex(2)))
            .check(isCompletelyBelow(allOf(withParent(withId(R.id.chat_layout)), withParentIndex(1))))
        onView(allOf(withParent(withId(R.id.chat_layout)), withParentIndex(2)))
            .check(isCompletelyLeftOf(allOf(withParent(withId(R.id.chat_layout)), withParentIndex(1))))

        // whisper and chatgpt
        onView(allOf(withParent(withId(R.id.chat_layout)), withParentIndex(3)))
            .check(isCompletelyBelow(allOf(withParent(withId(R.id.chat_layout)), withParentIndex(2))))
        onView(allOf(withParent(withId(R.id.chat_layout)), withParentIndex(3)))
            .check(isCompletelyRightOf(allOf(withParent(withId(R.id.chat_layout)), withParentIndex(2))))

        // whisper and chatgpt
        onView(allOf(withParent(withId(R.id.chat_layout)), withParentIndex(4)))
            .check(isCompletelyBelow(allOf(withParent(withId(R.id.chat_layout)), withParentIndex(3))))
        onView(allOf(withParent(withId(R.id.chat_layout)), withParentIndex(4)))
            .check(isCompletelyLeftOf(allOf(withParent(withId(R.id.chat_layout)), withParentIndex(3))))
    }

    @Test
    fun setupListeners_fourClicksTranslateButton_recordButtonEnabledStopButtonDisabledTranslateButtonEnabled() {
        // Given
        launchFragmentInContainer<ConversationFragment>(themeResId = R.style.Theme_FluentSpeak)

        // When
        onView(withId(R.id.translate_button)).perform(click())
        Thread.sleep(MILLISECONDS)
        onView(withId(R.id.translate_button)).perform(click())
        Thread.sleep(MILLISECONDS)
        onView(withId(R.id.translate_button)).perform(click())
        Thread.sleep(MILLISECONDS)
        onView(withId(R.id.translate_button)).perform(click())
        Thread.sleep(MILLISECONDS)

        // Then
        onView(withId(R.id.record_button)).check(matches(isEnabled()))
        onView(withId(R.id.stop_button)).check(matches(isNotEnabled()))
        onView(withId(R.id.translate_button)).check(matches(isEnabled()))
    }

    @Test
    fun setupListeners_fourClicksRecordButtonOneClickStopButton_recordButtonEnabledStopButtonDisabledTranslateButtonEnabled() {
        // Given
        launchFragmentInContainer<ConversationFragment>(themeResId = R.style.Theme_FluentSpeak)

        // When
        onView(withId(R.id.record_button)).perform(click(), click(), click(), click())
        Thread.sleep(MILLISECONDS)
        onView(withId(R.id.stop_button)).perform(click())
        Thread.sleep(MILLISECONDS)

        // Then
        onView(withId(R.id.record_button)).check(matches(isEnabled()))
        onView(withId(R.id.stop_button)).check(matches(isNotEnabled()))
        onView(withId(R.id.translate_button)).check(matches(isEnabled()))

        // whisper
        onView(allOf(withParent(withId(R.id.chat_layout)), withParentIndex(1))).check(isCompletelyBelow(withText("You are a helpful assistant.")))

        // whisper and whisper
        onView(allOf(withParent(withId(R.id.chat_layout)), withParentIndex(2)))
            .check(isCompletelyBelow(allOf(withParent(withId(R.id.chat_layout)), withParentIndex(1))))

        // whisper and whisper
        onView(allOf(withParent(withId(R.id.chat_layout)), withParentIndex(3)))
            .check(isCompletelyBelow(allOf(withParent(withId(R.id.chat_layout)), withParentIndex(2))))
        onView(allOf(withParent(withId(R.id.chat_layout)), withParentIndex(3)))
            .check(isCompletelyLeftOf(allOf(withParent(withId(R.id.chat_layout)), withParentIndex(2))))
    }

    @Test
    fun setupListeners_twoImmediateClicksRecordButton_noRuntimeExceptionStopFailed() {
        // Given
        launchFragmentInContainer<ConversationFragment>(themeResId = R.style.Theme_FluentSpeak)

        // When
        onView(withId(R.id.record_button)).perform(doubleClick())

        // Then
    }

    @Test
    fun setupListeners_twoImmediateClicksTranslateButton_noRuntimeExceptionStopFailed() {
        // Given
        launchFragmentInContainer<ConversationFragment>(themeResId = R.style.Theme_FluentSpeak)

        // When
        onView(withId(R.id.translate_button)).perform(doubleClick())

        // Then
    }

    @Test
    fun setupListeners_executeWithoutDataSource_noSetDataSourceFDFailedError() {
        // Given
        val scenario = launchFragmentInContainer<ConversationFragment>(themeResId = R.style.Theme_FluentSpeak)

        // When
        scenario.onFragment {
            it.configurePlayer()
        }

        // Then
    }
}