# Wireless debugging (no more USB hotplugging)

One-time setup per phone, using Android's built-in Wireless debugging (Android
11+ only — both test phones qualify: the Android 16 phone and the Android 12
phone). Once paired, `scripts/deploy.sh` works over Wi-Fi with no cable.

## Prerequisites

- Phone and Mac on the **same Wi-Fi network**.
- Developer options enabled on the phone (Settings → About phone → tap
  "Build number" 7 times, if not already on).
- `adb` from the Android SDK: `$HOME/Library/Android/sdk/platform-tools/adb`
  (this is what `scripts/deploy.sh` already resolves automatically).

## One-time pairing (per phone)

1. Phone: **Settings → System → Developer options → Wireless debugging** →
   turn it on.
2. Tap **"Pair device with pairing code"**. This shows a *pairing* IP:port
   (different from the connect address below) and a 6-digit code —
   both are single-use and regenerate each time you open this screen.
   **Don't close this popup until the `adb pair` command below succeeds** —
   closing it invalidates the code/port shown, and you'll have to reopen it
   and get a fresh one (an easy mistake to make while waiting).
3. From the Mac:
   ```
   adb pair <pairing-ip>:<pairing-port> <6-digit-code>
   ```
   e.g. `adb pair 192.168.1.152:33861 402875`.
4. On success, adb prints `Successfully paired to <ip>:<port> [guid=...]`.
   Pairing persists across reboots — you only need to redo this if Wireless
   debugging is turned off/revoked on the phone, not every session.

## Connecting (per phone, per session)

Back on the main **Wireless debugging** screen (not the pairing dialog),
there's a second IP:port near the top — the actual **connect** address,
on a different port than the pairing one:

```
adb connect <connect-ip>:<connect-port>
```

The connect port changes whenever the phone's Wireless debugging service
restarts, and the IP changes if its Wi-Fi lease renews — so if a previously
working address stops responding, just grab a fresh one from the phone's
screen and reconnect.

**We standardize on this manual `adb connect`, not mDNS auto-discovery** —
tried it, and it's unreliable across our two test phones: the Android 16
phone keeps advertising itself in the background so mDNS finds it fine, but
the Android 12 phone only advertises while its Wireless debugging *settings
screen* is open in the foreground (a quirk of that older Android/One UI
build), even though its underlying connection stays up regardless. Manual
connect works identically on both, so that's the one method documented here.

## Devices set up so far

| Phone | Model | Pairing guid |
|---|---|---|
| Android 16 (main test phone) | SM-S901U1 (Galaxy S22 Ultra) | `adb-R5CT41XRK0J-1eEbKi` |
| Android 12 (oldest test phone) | SM-G975U (Galaxy S10+) | `adb-R58M37HGYAV-vusHxe` |

## Gotcha: mDNS can still auto-reconnect a duplicate

Even though we don't rely on mDNS, `adb` scans for it automatically in the
background — so a phone can still show up **twice** in `adb devices -l`:
once as your manual IP:port entry, once as an mDNS-discovered
`adb-<GUID>._adb-tls-connect._tcp` entry, both pointing at the same physical
device. `scripts/deploy.sh` is unaffected (it targets one serial
explicitly), but Gradle's `connectedDebugAndroidTest` tasks treat every
entry in `adb devices` as a separate device and run the same test package on
all of them — which means running it *twice concurrently on the same
physical phone*, racing to install/uninstall the same APK. Symptoms:
`DELETE_FAILED_INTERNAL_ERROR` and "Process crashed" instrumentation
failures that have nothing to do with the code under test.

Before running any `connectedAndroidTest` task, check `adb devices -l` shows
exactly one entry per phone. If a duplicate shows up:

```
adb disconnect adb-<GUID>._adb-tls-connect._tcp
```

## Deploying with both phones connected at once

`scripts/deploy.sh` prompts for which phone to target (old/Android 12 or
new/Android 16), so both can stay connected simultaneously — no need to
disconnect one first. It looks up the currently-connected serial for the
chosen phone by matching its device model (`SM_G975U` for the Android 12
phone, `SM_S901U1` for the Android 16 phone) against `adb devices -l`, so it
keeps working even as the IP:port changes across sessions — as long as that
phone shows up in `adb devices -l` at all. If it doesn't, reconnect it per
the steps above.
