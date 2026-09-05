# Testing item 6 — searchable step-level activity log

Branch: `feat/step-activity-log`. Nothing is committed yet — this all needs
to pass before the feature code gets committed and merged into `personal`.

## 0. Before you start

Both phones should be connected — check:
```
adb devices -l
```
Should show exactly one entry per phone (`192.168.1.x:xxxx`, `device` state).
If a phone is missing or shows twice, see `wireless-debugging.md`.

## 1. Run the automated instrumented tests

```
./gradlew :presentation:connectedDebugAndroidTest :data:connectedDebugAndroidTest
```

Runs on every connected device automatically (both phones at once). Look
for `MachinePresenterTest` (`action1` + `stepStampRecording`) and
`MachineDatabaseMigratingTest` passing on both `SM-S901U1 - 16` and
`SM-G975U - 12`.

## 2. Deploy and manually test on each phone

```
scripts/deploy.sh
```
Pick 1 (old/Android 12) or 2 (new/Android 16). Repeat everything below on
the other phone once the first one checks out.

**a. Auto-logging** — start any timer, let a step's countdown reach zero on
its own without touching anything. Open the timer list's overflow menu (⋮)
→ **Step Log**. Confirm a row appears for that step tagged **Auto**, with
the right timer name, step name, and a timestamp close to now.

**b. Manual-logging via Next** — start a timer again, tap **Next** partway
through a step (before it finishes). Check Step Log → confirm a **Manual**
row for the step you just left.

**c. Previous/jump logs nothing** — on a running timer, tap **Previous**,
and separately try jumping to a different step via the step list. Check
Step Log both times → confirm neither action added a new row.

**d. Search filtering** — in Step Log, type into the search box at top.
Confirm the list live-filters to rows matching that text in either the
timer name or the step name, and clearing the box shows everything again.

**e. Survives timer deletion** — delete the timer you used for the tests
above (long-press in the timer list → delete). Reopen Step Log → confirm
its logged rows are still there.

## 3. Report back

Once both phones check out on every item above, let Claude know — the
"Manual test" section in `PERSONAL_PLAN.md` gets filled in for real, the
feature code gets committed, and `feat/step-activity-log` merges into
`personal`. If anything fails, report what you saw instead of what's
expected and it gets fixed before merging.
