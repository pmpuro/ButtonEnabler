package pmpuro.buttonenabler

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.Closeable

class ButtonEnabler(
    private val coroutineScope: CoroutineScope = MainScope(),
    private val action: suspend () -> Unit,
) : ClickHandler, Closeable {

    private val channel = Channel<Boolean>(Channel.CONFLATED)
    private val clicks = channel.consumeAsFlow()

    private val _enabled = MutableStateFlow(value = true)
    override val enabled: StateFlow<Boolean>
        get() = _enabled

    suspend fun handleClicks(): Throwable? {
        var result: Throwable? = null

        clicks
            .onEach {
                _enabled.emit(false)
                action()
                _enabled.emit(true)
            }
            .catch {
                result = it
                _enabled.emit(true)
            }
            .collect()

        return result
    }

    override fun launchClick() {
        coroutineScope.launch {
            runCatching { click() }
                .onFailure {
                    Log.e("ButtonEnabler", "failed to click")
                }
        }
    }

    override suspend fun click() = channel.send(true)
    override fun close() {
        channel.close()
    }
}
