# AndroidXroar

AndroidXroar is an Android app that packages the WebAssembly build of XRoar inside a native Android shell. The app uses an Android `WebView` to load the bundled XRoar web frontend and adds Android-specific file access so ROMs, configuration files, and software images can be selected from the device.

## What It Does

- Runs XRoar on Android using the bundled WebAssembly build in [app/src/main/assets/xroar](app/src/main/assets/xroar).
- Uses a native Android activity in [app/src/main/java/com/tazrog/xroar/MainActivity.kt](app/src/main/java/com/tazrog/xroar/MainActivity.kt) to host the emulator UI.
- Lets the user import ROM folders and emulator configuration files through Android document pickers.
- Preserves access to imported ROM folders and configuration files with persisted Android storage permissions.
- Supports Android display mode selection while keeping the emulator packaged entirely offline.
- Keeps emulator assets packaged with the app instead of requiring a separate web server.

## Project Layout

- [app](app): Android application module.
- [app/src/main/assets/xroar](app/src/main/assets/xroar): Bundled XRoar WebAssembly runtime and web assets.
- [xroar-1.10](xroar-1.10): Vendored XRoar source tree.
- [scripts/build_xroar_wasm.sh](scripts/build_xroar_wasm.sh): Helper script for rebuilding the WebAssembly artifacts copied into the Android assets directory.

## Building

Requirements:

- Android SDK installed and configured through Android Studio or `local.properties`.
- Java 11 available for Gradle and the Android toolchain.

Build the debug app with:

```bash
./gradlew assembleDebug
```

Build the release app with:

```bash
./gradlew assembleRelease
```

This project also defines a packaging task that copies the release APK to a predictable filename:

```bash
./gradlew packageNamedReleaseApk
```

If `keystore.properties` is present, the release build is signed with that keystore. Otherwise Gradle produces an unsigned release APK and the packaging task still copies it to the release folder.

The generated file is copied to [release](release) as `AndroidXroar.apk`.

## Release APK

After running:

```bash
./gradlew packageNamedReleaseApk
```

you can find the installable APK at [release](release). The `release` directory is created by the Gradle packaging task if it does not already exist.

## Download And Install On Android

1. Build the release APK with `./gradlew packageNamedReleaseApk`.
2. Open the [release](release) folder and copy `AndroidXroar.apk` to your Android device.
3. On the Android device, open the APK from the Files app or Downloads.
4. If Android blocks the install, allow installs from the app you used to open the APK when prompted.
5. Continue the installer prompts and finish the installation.

If you already have an older copy installed, Android may require you to uninstall it first if the signing key is different.

## ROMs And Configuration

XRoar requires system ROM images for the machines you want to emulate. Those ROMs are not included in this repository. Use your own legally obtained ROM images and import them through the app.

Imported ROM folders are stored via Android's document provider access, and imported configuration files are copied into the app's private storage so they remain available between launches.

The bundled XRoar source tree documents the expected ROM filenames and supported machines in [xroar-1.10/README](xroar-1.10/README).

## Using The App

1. Open the `Config` page first and use `Import ROM Folder` to select a folder containing your XRoar ROM files.
2. On the same page, optionally choose a startup machine, RAM size, and VDG type, then use `Apply And Reload` to restart with those overrides.
3. If you already use an `xroar.conf` file, use `Import Config` on the `Config` page to preload that file inside the app.
4. After ROM import or reload completes, return to the `Screen` page to use the emulator.
5. Open the `Media` page to insert disks or tapes, or use `Quick Load` to open files that XRoar can auto-detect.
6. On the `Screen` page, use the input toggle to switch between the on-screen keyboard and the gamepad.
7. The on-screen `CLEAR` key requires a long press.
8. Open `Settings` to change the gamepad side, choose one-button or two-button fire mode, adjust joystick behavior, edit keyboard keywords, and change display mode.
9. Use `Soft Reset` or `Hard Reset` from the top bar when you need to restart the emulated machine.

## Rebuilding The WASM Assets

If you update the vendored XRoar sources or want to regenerate the emulator runtime, use:

```bash
./scripts/build_xroar_wasm.sh
```

That script rebuilds the WebAssembly target and copies the generated `xroar.js` and `xroar.wasm` files into the Android asset bundle.

## Credit

This project is built on XRoar, the Dragon and Tandy 8-bit computer emulator created by Ciaran Anscomb.

- XRoar home page: https://www.6809.org.uk/xroar/
- Vendored source in this repository: [xroar-1.10](xroar-1.10)
- Original copyright notice from XRoar: Copyright 2003-2025 Ciaran Anscomb `<xroar@6809.org.uk>`

XRoar emulates systems including the Dragon 32/64, Tandy Colour Computer 1/2/3, and Tandy MC-10, along with related peripherals and media formats. AndroidXroar is an Android packaging and integration project; the underlying emulator technology and emulation work belong to the XRoar project and its author.

## License

AndroidXroar is licensed under the GNU General Public License, version 3. See [LICENSE](LICENSE).

## Warranty And Risk

AndroidXroar is provided without guarantees or warranty of any kind. You are responsible for how you use this software, and you use it entirely at your own risk.

XRoar is free software released under the GNU General Public License, version 3 or later, as described in the bundled XRoar documentation and license files:

- [LICENSE](LICENSE)
- [xroar-1.10/COPYING.GPL](xroar-1.10/COPYING.GPL)
- [xroar-1.10/README](xroar-1.10/README)

Review the licenses for both AndroidXroar and the bundled XRoar sources before redistributing builds from this repository.
