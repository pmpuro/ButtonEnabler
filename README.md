
# ButtonEnabler

[![Release](https://jitpack.io/v/pmpuro/ButtonEnabler.svg)](https://jitpack.io/#pmpuro/ButtonEnabler) 

ButtonEnabler is a utility to disable and enable automatically a button, or any UI element, 
while a click is being processed.

## Dependency

Add the repository, if not already, in your root build.gradle at the end of repositories:
```groovy
allprojects {
		repositories {
			//...
			maven { url 'https://jitpack.io' }
		}
	}
```

And, add the dependency in your module's build.gradle:
```groovy
dependencies {
	        implementation 'com.github.pmpuro.ButtonEnabler:ButtonEnabler:0.1.4'
	}
```

## Usage

The following code demonstrate how you could use the ButtonEnabler (handler) in your composable functions.

```kotlin
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
```

ButtonEnabler takes a coroutine scope and action. The action is called when the launchClick, 
or click, is called on it. It can be instantiated, for example, in an Activity.

````kotlin
private val clickHandler = ButtonEnabler(lifecycleScope) {
    longLastingAction()
}
````

And, it can then be provided to the set of composable functions of your UI with an ambient.

```kotlin
val AmbientClickHandler = ambientOf<ClickHandler> { error("no clicker") }
```

The ambient is set by calling the Providers composable function as follows:

```kotlin
ClickerExampleAppTheme {
    Providers(AmbientClickHandler provides clickHandler) {
        Surface(color = MaterialTheme.colors.background) {
            Greeting("Android")
        }
    }
}
```

And finally, you need to call `handleClicks()` function to start processing clicks. 
See that `handleClicks()` returns with an exception thrown in the action.
To finish call `close()` function, for example in your Activity's `onDestroy()` life cycle function.

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { 
        //... 
    }

    lifecycleScope.launchWhenCreated {
        clickHandler.handleClicks()
    }
}

override fun onDestroy() {
    super.onDestroy()
    clickHandler.close()
}
```


## License

```
Copyright 2021 Panu Puro

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```