<p align="center"><img src="app/src/main/ic_launcher-playstore.png" width="150"></p>
<h1 align="center"><b>Super Smart Key</b></h1>
<p align="center">
<a href="https://github.com/dpither/super-smart-key/releases/latest">
    <img alt="GitHub Release" src="https://img.shields.io/github/v/release/dpither/super-smart-key"></a>

  <a href="https://developer.android.com/about/versions/pie">
    <img alt="Android 9+" src="https://img.shields.io/badge/Android-9+-3DDC84?logo=android"></a>

</p> 

Have you ever wished that your phone would automatically lock when you walked away from it? Me
neither, but my
Dad did, so this project exists to solve that problem.

Super Smart Key is an Android 9+ app that turns connected Bluetooth devices into smart keys that
automatically lock
your device when they move out of a configurable range.

## Download

<a href="https://github.com/dpither/super-smart-key/releases/latest"><img height=70 alt="Get it on Github" src="images/get_it_on_github.png"></a>

Super Smart Key is also in closed testing on Google Play, if you would like to contribute to its
full release there send your gmail to dylanpitherdev@gmail.com.

## Screenshots

<p align="center">
<img src="images/home.jpg" width=32%/> <img src="images/settings.jpg" width=32%/> <img src="images/available_keys.jpg" width=32%/>
<img src="images/key_connected.jpg" width=32%/> <img src="images/service_running.jpg" width=32%/> <img src="images/key_out_of_range.jpg" width=32%/>
</p>

## How It Works

Super Smart Key uses Bluetooth RSSI (Received Signal Strength Indicator) to approximate the distance
to a Bluetooth
device. Currently, the app requires Bluetooth devices to be already connected to the mobile device
to be considered
a potential key candidate.

To get started, tap the link button in the bottom right to link to an available key. Since the
distance approximation
can be influenced by obstructions or device output variability, it's helpful to observe the RSSI of
the key from a
desirable position. Then, set a suitable RSSI Threshold in settings, accessible via the gear button
in the top right,
before starting the service.

Once the service is started, a notification will appear to indicate its presence. The service will
periodically check for the
RSSI of the linked key and if the read value is below the RSSI Threshold, or the key has
disconnected, it will lock the
device. The service can be stopped at any time from the notification or by pressing the button
within the app.

> [!NOTE]
> Not all Bluetooth devices support RSSI reading. Verify the key status is `Connected` and the RSSI
> value is changing before starting the service.

> [!TIP]
> Bluetooth beacons are solid key candidates. In fact, this project primarily used a Galaxy
> SmartTag2 for testing, however, they currently do not work on Android 12+. 

## Settings

- RSSI Threshold [-100, -40] (dBm)
    - The minimum signal strength required to keep the device unlocked. A higher RSSI threshold
      generally requires a shorter distance between the device and key before locking.
- Grace Period [10, 120] (s)
    - The minimum time between consecutive locks. A higher grace period allows more time to turn off
      the service in the event of key loss.
- Polling Rate [1, 30] (s)
    - The frequency of key signal strength checks. A higher polling rate uses less system resources
      but makes the service less responsive to changes in key position.

## Permissions

- Bluetooth (`BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT` on Android 12+ otherwise, `BLUETOOTH`,
  `BLUETOOTH_ADMIN`, `ACCESS_FINE_LOCATION`)
    - To connect to Bluetooth devices and use them as keys.
- Services (`FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_CONNECTED_DEVICE`)
    - To communicate with keys and run the key service in the background.
- Notifications (`POST_NOTIFICATIONS`)
    - To indicate when the key service is running in the background.
- Device Admin (`force-lock`)
    - To allow keys to lock the device when out of range.

## Under The Hood

Super Smart Key is developed using [Android Studio](https://developer.android.com/studio), it
follows
the [Model-View-ViewModel (MVVM)](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93viewmodel)
design pattern for maintainability and robustness.
The UI is built with [Jetpack Compose](https://developer.android.com/compose), and uses a
single-activity architecture
with [compose navigation](https://developer.android.com/develop/ui/compose/navigation).

#### Dependencies

- [Jetpack Compose](https://developer.android.com/compose) (UI toolkit)
- [Compose Navigation](https://developer.android.com/develop/ui/compose/navigation) (UI navigation)
- [Google Fonts](https://developer.android.com/develop/ui/compose/text/fonts#downloadable-fonts) (UI
  fonts)
- [Accompanist Permissions](https://google.github.io/accompanist/permissions/) (Runtime permission
  handling)
- [Preferences DataStore](https://developer.android.com/topic/libraries/architecture/datastore#preferences-datastore-dependencies) (
  Settings persistence)
- [Hilt](https://dagger.dev/hilt/) (Dependency injection)
- [Process Lifecycle](https://developer.android.com/reference/androidx/lifecycle/ProcessLifecycleOwner) (
  Application lifecycle tracking)

## License

Super Smart Key is licensed under the [GNU General Public License v3.0 (GPLv3)](LICENSE).
