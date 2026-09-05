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

In practice this often isn't even necessary — once a phone is paired, `adb`'s
mDNS auto-discovery finds and connects to it on its own (shows up in
`adb devices -l` as `adb-<GUID>._adb-tls-connect._tcp`, alongside the plain
IP:port entry). If a device just doesn't show up in `adb devices`, run the
`adb connect` above with a fresh IP:port from the phone's screen — the
connect port can change if the phone's Wi-Fi IP lease renews.

## Devices set up so far

| Phone | Model | Pairing guid |
|---|---|---|
| Android 16 (main test phone) | SM-S901U1 (Galaxy S22 Ultra) | `adb-R5CT41XRK0J-1eEbKi` |
| Android 12 (oldest test phone) | SM-G975U (Galaxy S10+) | `adb-R58M37HGYAV-vusHxe` |

## Deploying with both phones connected at once

`scripts/deploy.sh` now prompts for which phone to target (old/Android 12 or
new/Android 16), so both can stay connected simultaneously — no need to
disconnect one first. It looks up the currently-connected serial for the
chosen phone by matching its device model (`SM_G975U` for the Android 12
phone, `SM_S901U1` for the Android 16 phone) against `adb devices -l`, so it
keeps working even as the IP:port changes across DHCP lease renewals — as
long as that phone shows up in `adb devices -l` at all. If it doesn't,
re-pair/reconnect it per the steps above.
