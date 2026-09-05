# Testing item 8 — import a timer from a JSON file

Branch: `feat/timer-json-import`. Nothing is committed yet — this all needs
to pass before the feature code gets committed and merged into `personal`.

## Sample file to import

`rubber-duck-debugging-ritual.json` — a small, hand-written 3-step timer
(Voice → Beep+Vibration → Voice, about 1:40 total). Viewable and copyable
here: https://claude.ai/code/artifact/4a94979a-2fae-4321-8f01-8c79613a7512

Get it onto the phone however's easiest — copy the JSON from that page into
a text file saved as `rubber-duck-debugging-ritual.json` in Downloads,
AirDrop/email it to yourself, or drop it in Drive. Anywhere the system file
picker can browse to works, Drive included (that's the point of using
`ACTION_OPEN_DOCUMENT` — no separate Drive integration needed).

A checklist version of everything below, with checkboxes that persist in
your browser: https://claude.ai/code/artifact/a38ec0f8-3e88-4ef1-9e89-29b27adee6f9

## 0. Before you start

Both phones should be connected — check:
```
adb devices -l
```
Should show exactly one entry per phone (`192.168.1.x:xxxx`, `device` state).
If a phone is missing or shows twice, see `wireless-debugging.md`.

## 1. Deploy and manually test on each phone

```
scripts/deploy.sh
```
Pick 1 (old/Android 12) or 2 (new/Android 16). Repeat everything below on
the other phone once the first one checks out.

**a. Entry point** — in the main **Timers** list, open the overflow menu
(⋮, top-right). Confirm **Import Timer** appears there, alongside **Records**.

**b. File picker** — tap **Import Timer**. Confirm the system file picker
opens. Navigate to and select `rubber-duck-debugging-ritual.json`.

**c. Folder picker** — confirm a **Choose a folder** dialog appears next,
listing your real folders (whatever you already have, e.g. Default plus any
you've created).

**d. Import lands correctly** — pick a folder. Confirm:
- A snackbar reads `Imported "Rubber Duck Debugging Ritual"`.
- The new timer shows up in the folder you picked, named
  "Rubber Duck Debugging Ritual", with 3 steps and roughly a 1:40 total
  duration shown in the list.

**e. Run it** — start the timer. Confirm:
- Step 1 ("Summon the duck", 5s) speaks a voice line.
- Step 2 ("Confess the bug out loud", 1:30) beeps twice and vibrates once.
- Step 3 ("Thank the duck and move on", 5s) speaks a voice line.

**f. Cancel does nothing** — open **Import Timer** again, then back out of
the file picker without selecting anything. Confirm nothing happens — no
snackbar, no crash, no phantom timer.

**g. Bad file doesn't crash** — open **Import Timer** again and pick some
other file that isn't a timer export (a photo, a random `.txt`, whatever's
handy). Confirm a snackbar says the file isn't a valid timer, and the app
doesn't crash.

## 2. Report back

Once both phones check out on every item above, let Claude know — the
"Manual test" section in `PERSONAL_PLAN.md` gets filled in for real (with
`rubber-duck-debugging-ritual.json`'s content pasted in as the documented
example the plan asked for), the feature code gets committed, and
`feat/timer-json-import` merges into `personal`. If anything fails, report
what you saw instead of what's expected and it gets fixed before merging.
