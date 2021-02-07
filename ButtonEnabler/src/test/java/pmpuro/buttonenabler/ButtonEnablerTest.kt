package pmpuro.buttonenabler

import com.google.common.truth.Truth
import junit.framework.Assert
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import java.io.IOException

private const val KEY_CLICKED = "CLICKED"
private const val KEY_DISABLED = "DISABLED"
private const val KEY_ENABLED = "ENABLED"

@OptIn(ExperimentalCoroutinesApi::class)
class ButtonEnablerTest {

    private val events = mutableListOf<String>()

    @Test
    fun `the default value is true`() = runBlockingTest {
        ButtonEnabler(this) {}
            .apply {
                Truth.assertThat(enabled.value).isEqualTo(true)
                close()
                handleClicks()
                Truth.assertThat(enabled.value).isEqualTo(true)
            }
    }

    @Test
    fun `handler calls the action`() = runBlockingTest {
        val tested = ButtonEnabler(this) { events.add(KEY_CLICKED) }

        val starter = async(this.coroutineContext) {
            tested.handleClicks()
        }

        with(tested) {
            click()
            close()
        }

        starter.await()

        Truth.assertThat(events).containsExactlyElementsIn(
            listOf(
                KEY_CLICKED,
            )
        )
    }

    @Test
    fun `handler emits disabled while click handled`() = runBlockingTest {
        val tested = ButtonEnabler(this) { events.add(KEY_CLICKED) }

        val collector = async(this.coroutineContext) {
            tested
                .enabled
                .take(3)
                .collect {
                    events.addEvent(it)
                }
        }

        val starter = async(this.coroutineContext) {
            tested.handleClicks()
        }

        tested.apply {
            click()
            close()
        }

        listOf(collector, starter).awaitAll()

        Truth.assertThat(events).containsExactlyElementsIn(
            listOf(
                KEY_ENABLED,
                KEY_DISABLED,
                KEY_CLICKED,
                KEY_ENABLED
            )
        )
    }

    @Test
    fun `action tolerates exceptions but handleClicks returns`() = runBlockingTest {
        var actionThrows = true
        val tested = ButtonEnabler(this) {
            events.add(KEY_CLICKED)
            if (actionThrows) throw IOException("example exception")
        }

        val collector = async(this.coroutineContext) {
            tested
                .enabled
                .take(3)
                .collect {
                    events.addEvent(it)
                }
        }

        val starter = async(this.coroutineContext) {
            tested.handleClicks()
        }

        tested.apply {
            actionThrows = true
            click()
            actionThrows = false
            kotlin.runCatching {
                click()
            }.onSuccess {
                Assert.fail("Not happens")
            }
            close()
        }

        listOf(collector, starter).awaitAll()

        Truth.assertThat(events).containsExactlyElementsIn(
            listOf(
                KEY_ENABLED,

                KEY_DISABLED,
                KEY_CLICKED,
                KEY_ENABLED,
            )
        )
    }

    private fun MutableList<String>.addEvent(enabled: Boolean) =
        add(if (enabled) KEY_ENABLED else KEY_DISABLED)
}
