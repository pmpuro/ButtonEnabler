package pmpuro.clicker.clickerexampleapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.ambientOf
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import pmpuro.buttonenabler.ButtonEnabler
import pmpuro.buttonenabler.ClickHandler
import pmpuro.clicker.clickerexampleapp.ui.theme.ClickerExampleAppTheme

val AmbientClickHandler = ambientOf<ClickHandler> { error("no clicker") }

class MainActivity : AppCompatActivity() {

    private val clickHandler = ButtonEnabler(lifecycleScope) {
        longLastingAction()
    }

    private suspend fun longLastingAction() {
        Log.i("longLastingAction", "begin")
        delay(1700L)
        Log.i("longLastingAction", "end")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClickerExampleAppTheme {
                Providers(AmbientClickHandler provides clickHandler) {
                    // A surface container using the 'background' color from the theme
                    Surface(color = MaterialTheme.colors.background) {
                        Greeting("Android")
                    }
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            clickHandler.handleClicks()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clickHandler.close()
    }
}

@Composable
fun Greeting(name: String) {
    val handler = AmbientClickHandler.current
    val buttonEnabled = handler.enabled.collectAsState(initial = true)

    Button(
        onClick = {
            handler.launchClick()
        },
        enabled = buttonEnabled.value
    ) {
        Text(text = "Hello $name!")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ClickerExampleAppTheme {
        Greeting("Android")
    }
}