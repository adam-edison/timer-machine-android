# Personal fork — quick wins plan

Working branch: `personal`, cut from `develop` (this fork's main). Not intended for
upstream contribution. Stays a private, GPLv3-compliant modified version for personal
use only (see licensing note at the bottom).

## Status

- [x] [0. Personal branding](#0-personal-branding-do-first)
- [x] [1. Total timer duration shown in the list](#1-total-timer-duration-shown-in-the-list)
- [x] [2. Confirm behavior — count down, then wait for manual confirmation, with nagging](#2-confirm-behavior--count-down-then-wait-for-manual-confirmation-with-nagging)
- [x] [3. Day-of-week condition on a step](#3-day-of-week-condition-on-a-step)
- [ ] [4. Time-of-day range condition](#4-time-of-day-range-condition)
- [ ] [5. QR-scan dismiss mode for HALT](#5-qr-scan-dismiss-mode-for-halt)
- [ ] [6. Searchable step-level activity log](#6-searchable-step-level-activity-log)
- [ ] [7. Search timers by name](#7-search-timers-by-name)
- [ ] [8. Import a timer from a JSON file](#8-import-a-timer-from-a-json-file-including-google-drive)
- [ ] [9. Composable timers](#9-composable-timers--run-a-saved-timer-as-a-step-n-times)
- [ ] [10. Local music playlist](#10-local-music-playlist--searchable-persists-across-steps-layers-with-alerts)
- [ ] [11. Proactive, offline-capable TTS pre-baking](#11-proactive-offline-capable-tts-pre-baking)
- [ ] [12. Self-hosted high-quality TTS voice](#12-self-hosted-high-quality-tts-voice)
- [ ] [13. Sound sequencing across behaviors on one step](#13-sound-sequencing-across-behaviors-on-one-step)

Check a box and flip its section's `Status:` line to `done` in the same commit
that merges the feature branch into `personal`.

## Workflow

- Each feature below gets its own branch, cut from `personal`: `feat/<name>`.
- Build and manually test it, then merge into `personal` with `git merge --no-ff feat/<name>`.
- Commits within a feature branch follow [Conventional Commits](https://www.conventionalcommits.org/):
  `feat(scope): summary`, `fix(scope): summary`, etc.
- Cutting from `personal` (not from `develop`) means manual testing always exercises
  the real running app — every previously merged personal feature included — instead
  of a stripped-down build missing everything but the one feature under test.
- After this initial batch of quick wins, later work (the bigger personal-only
  features — playlists, TickTick, AI TTS, composable timers, etc.) goes straight onto
  `personal` as plain conventional commits, no separate feature branch per item.
- After each feature is verified working on-device, its "Manual test" section below
  gets filled in and committed together with the feature's merge.

## 0. Personal branding (do first)

Branch: `feat/personal-branding`

So a sideloaded build is unmistakably not the official app: a new product flavor,
alongside the existing `dog` / `google` / `other` flavors already defined in
`app/build.gradle.kts:73-88`.

**What:**
- New flavor `personal` in the `market` dimension, with its own `applicationId`
  suffix (e.g. `.personal`) so it installs side-by-side with the Play Store version
  without a signature/package clash.
- Flavor-specific resource overrides in a new `app/src/personal/res/` source set:
  - `values/strings.xml` → override `app_name`. Placeholder: `Timer Machine (Mine)`,
    swap for a real name anytime by editing this one file.
  - `mipmap-*/ic_launcher*` (and adaptive icon layers) → a distinct launcher icon.
    Placeholder: the existing launcher icon with a tint/badge applied, just enough
    to be visually distinct at a glance — swap for a real icon anytime by
    replacing the files in this flavor's `res/` folder.
- No changes to any shared file — everything lives in the new flavor's own source
  set, so this can never conflict with an upstream merge.

**How it was actually built:**
- `FlavorData.Flavor` (`app-base`) only has `Dog`/`Google`/`Other` — adding a
  `Personal` case would mean touching a shared file. Instead
  `app/src/personal/.../FlavorDataImpl.kt` reports `Flavor.Other`, same as the
  `other` flavor: it only gates Play-review-specific behavior (billing/in-app
  review) off, which is correct for a sideloaded build too.
- `"personalImplementation"(project(":app-analytics-fake"))` added alongside
  `dog`/`other` in `app/build.gradle.kts` — needed for the same fake-analytics
  Hilt binding those flavors use; without it the DI graph doesn't compile for
  this variant.
- The launcher icon is a vector adaptive icon (`ic_launcher_background.xml` +
  `ic_launcher_foreground.xml` in `app-base`), not flat PNGs, so the tint was
  applied by overriding just `app/src/personal/res/drawable/ic_launcher_background.xml`
  with a solid near-black fill — same clock glyph foreground, clearly different
  background color/pattern at a glance. Legacy pre-API26 `app_icon_square`/
  `app_icon_round` PNG fallbacks were left untouched (not used on the target
  device, min SDK 23 devices would still see the stock icon there).

**Status:** done

**Manual test:** Built `feat/personal-branding` off `develop`, ran
`./gradlew installPersonalDebug` over USB. Installed and launched alongside the
existing Play Store build (`io.github.deweyreed.timer.google`) as a separate
package (`io.github.deweyreed.timer.personal`) with no clash. Confirmed on
device: app name shows "Timer Machine (Mine)" and the launcher icon shows the
tinted near-black background. Merged into `personal` via
`git merge --no-ff feat/personal-branding`.

## 1. Total timer duration shown in the list

Branch: `feat/timer-duration-display`

**What:** show each timer's total runtime (respecting loops, nested groups, and
skip logic) under its name in both the list and grid layouts, so you don't have to
open a timer to see how long it runs.

**Where it turned out easier than expected:** `TimerEntity.getTotalTime()` already
exists (`presentation/.../stream/TimerMachineHelper.kt:410`) and already walks the
step tree correctly (loops, groups, skip). It's just never been wired into the list
screen, because the list screen deliberately loads only the lightweight `TimerInfo`
(id/name/folderId — see the DAO query `Daos.kt:27-31`) and skips loading full step
data for performance.

**Design call:** add a second reactive query (`TimerDao.getTimersFlow`, mirroring
the existing `getTimerInfoFlow`) that loads full `TimerEntity` rows per folder,
purely to compute durations, combined with the existing lightweight `timerInfo`
stream in `TimerViewModel`. This does mean the list loads full step data for every
visible timer, which is the exact thing the lightweight query was written to avoid
— acceptable at personal scale (a personal timer list is realistically a few dozen
entries, not thousands), revisit only if list scroll or load time is ever
noticeably worse after this lands.

**Touches:** `TimerRepository.kt` (new interface method) · `Daos.kt` (new query) ·
`TimerRepositoryImpl.kt` · new `GetTimersFlow.kt` use case · `TimerViewModel.kt`
(new combined LiveData) · `MutableTimerItem.kt` (new field) · `TimerFragment.kt` ·
`CollapsedViewHolder.kt` · `list_item_timer_collapsed.xml` and
`list_item_timer_collapsed_grid.xml` (new duration TextView, reusing the existing
`Long.produceTime()` formatter from `app-base/.../TimeConverter.kt:29`).

**Styling:** duration text is 18sp (up from the initial caption size) in a
pale blue that's tuned per theme for legibility — `app-timer-list/src/main/
res/values/colors.xml` (`#4A78B5`, light mode) and `values-night/colors.xml`
(`#90CAF9`, Material Blue 200, dark mode), following the existing per-module
`values`/`values-night` color pattern already used elsewhere in the codebase
(e.g. `app-timer-edit`'s `step_info_background`).

**Effort:** half a day to a day.

**Status:** done

**Manual test:** Built `feat/timer-duration-display` off `develop`, installed
`installDogDebug` over USB (launched directly via `adb shell am start` to
avoid the dog/google flavors sharing an identical name and icon). Confirmed
on-device: every timer in both list and grid view shows its total duration
under its name, styled larger and pale blue, legible in both light and dark
theme. Merged into `personal` via `git merge --no-ff feat/timer-duration-display`.

## 2. Confirm behavior — count down, then wait for manual confirmation, with nagging

Branch: `feat/halt-nag-interval`

**What:** originally scoped as "add a nag interval to the existing HALT behavior."
Turned out HALT doesn't do what it sounds like it should for this use case — see
below — so this became a new, separate behavior instead: **Confirm**. A step with
Confirm counts down its configured length exactly like a normal step (any other
behaviors on it — Beep/Voice/Half/Count — fire on their usual schedule during that
countdown). Once the countdown reaches zero, the step doesn't advance: it holds
indefinitely, plays its alert (Voice/Beep) once, and — if a nag interval is set —
replays that alert every N seconds until you manually move on (the existing
"Next" action, available from the running notification and from the running
screen's configurable action buttons). 0 means "alert once when done, never nag."

**Why not just extend HALT:** read `TimerMachine.kt` before writing anything and
found HALT doesn't count down through the step's length at all — the moment a
HALT step starts, the whole step becomes an indefinite up-counting stopwatch from
time zero (`StopwatchTask`, chosen unconditionally). The step's length field is
stored and shown in the editor but never used at runtime for a HALT step; it
always has been "halt instantly," never "count down, then halt." Retrofitting a
lead-in countdown onto HALT would have silently changed the runtime behavior of
every HALT step in every timer already built, for a behavior most people
presumably use specifically because it's instant (e.g. a bare confirmation
step with 0:00 length). Building a separate **Confirm** behavior keeps HALT
exactly as-is and adds the new "count down, then wait" semantics as an opt-in.

**Mutually exclusive with HALT:** both behaviors claim what happens when a step's
countdown timing ends, so a step can only have one, not both — enforced in
`EditableBehaviourLayout.kt`'s add-behavior menu (adding one hides the other).

**Design:**
- New `BehaviourType.CONFIRM`, `ConfirmAction(nagIntervalSeconds: Int = 0,
  content: String = "")` in `BehaviourEntity.kt` (`domain`) — `str1` holds the
  interval (same int-in-a-string pattern as `NotificationAction`/`CountAction`),
  `str2` holds the spoken content, same shape as `VoiceAction.content`/`content2`.
  No new Room column or JSON adapter change needed — `str1`/`str2` are already
  generic per-behavior-type columns, and `BehaviourDataJsonAdapter` already
  round-trips them for every type.
- Confirm has its own spoken line, independent of any separate `VOICE` behavior
  on the step — default `ConfirmAction.DEFAULT_CONTENT = "Did you finish
  {step_name}?"`, editable to anything via the same `{variable}` picker Voice's
  "new variables" mode uses (see the variable-substitution trap below for why
  it must go through `content2`, not `content`).
- New `ConfirmTask` (`presentation/.../stream/task/ConfirmTask.kt`): a task that
  behaves like `CountDownTimerTask` for `length`, then — instead of finishing and
  letting the step advance — switches internally into an indefinite phase like
  `StopwatchTask`'s, ticking once a second via a dedicated `onConfirmTick`
  callback (not the shared per-second `TickListener` list used by
  Beep/Half/Count, which only fire during the countdown phase — that's what
  makes "count down normally, then wait" true without touching those listeners
  at all). `TimerMachine.toTask()` picks `ConfirmTask` when a step has a
  `CONFIRM` behavior, ahead of the existing `HALT` → `StopwatchTask` check.
  `TimerMachine.Listener.confirmAlert(timerId, index)` replaces the alert-replay
  logic that was briefly named `nagHalt` during the false start above; it's
  called once when the wait begins and again on every nag interval.
- `MachinePresenter.confirmAlert()` replays just that step's Beep + Confirm's
  own voice line — not a full `startBehaviours()` re-run, so it doesn't restart
  Music, Vibration, the fullscreen overlay, or the flashlight, which keep
  running undisturbed through the wait.

**Watch for — three traps found the hard way, in the order hit:**
1. The codebase flags, in a comment (`TimerMoreEntity` in `TimerEntity.kt`),
   several places that need updating for any new *`more`* field (`TestData`,
   `TimerMoreMapper`, `MappersTest`, `OneFragment`, `EditActivity`) — didn't
   apply here since Confirm's data lives in the existing generic `str1`/`str2`
   columns, not a new `more` field, but worth rechecking for any future
   behavior field that doesn't fit that pattern.
2. **The behavior settings popup is implemented twice.** Tapping a behavior
   chip to configure it is handled once in `UpdateStepDialog.kt` and again,
   separately, in `EditActivity.kt` (`EditableStep.kt` just forwards to
   whichever one owns the click). The `Create Timer` screen's inline step list
   uses `EditActivity`'s copy; adding the new case to only `UpdateStepDialog.kt`
   first meant the settings item silently didn't show up (only "Delete" did)
   until the second copy was found and patched too. Same trap will apply to any
   future behavior with its own settings UI.
3. **Two parallel variable-substitution systems, only one understands
   `{variable}` tokens.** `TimerMachineHelper.generateVoiceContent()` has two
   completely separate code paths: reading `VoiceAction.content` only replaces
   the legacy `$variable`-style tokens (`REPLACER_STEP_NAME = "$step_name"`,
   etc.); reading `VoiceAction.content2` routes into a different function
   (`variableToValue`) that's the only one that understands the modern
   `{variable}` tokens (`{step_name}`, `{SName}`, ...) that Voice's "Variables"
   picker actually inserts. First pass wired Confirm's content through
   `content` — the default `"Did you finish {step_name}?"` came out spoken
   literally, brace and all, since nothing on that path recognizes `{...}`.
   Fixed by always constructing the throwaway `VoiceAction` with `content2 =`
   (never `content =`) and opening `VoiceVariableDialog` (not the older plain
   `VoiceDialog`) as Confirm's content editor — Confirm has no legacy data to
   support, so there was no reason to keep the old token path reachable at all.
4. **`enableTone()` doesn't make a sound — it only arms the Beeper.** The actual
   beep happens in `playTone()`, called once per second by `BeepTickListener`
   during a normal countdown. `confirmAlert()` initially only called
   `enableTone()` (copied straight from `startBehaviours()`), so Beep silently
   armed and never fired on a nag. Fixed by also calling `view?.playTone()`
   right after arming it — one beep pulse per nag event, since there's no
   per-second ticking during the confirm wait to spread a multi-beep `count`
   across.

**Effort:** ended up more than the original 0.5–1 day estimate for a plain nag
field, since it became a new task-lifecycle concept in the timer engine instead
of a one-field addition to an existing behavior, plus its own spoken-content
field wired through the correct variable-substitution path.

**Status:** done

**Manual test:** Built on `feat/halt-nag-interval` (cut from `personal`, not
`develop`, per the updated workflow below), deployed via `scripts/deploy.sh`
(`personal` flavor) to a physical device. Created a step with Confirm (short
length, short nag interval) plus Beep. Confirmed on-device:
- Counts down normally, then holds and starts counting up (the confirm wait)
  until "Next" is pressed — never auto-advances.
- Nag interval > 0 repeats the alert every N seconds while waiting; interval = 0
  asks once when the wait begins and never repeats.
- "Next" (notification action / running-screen action button) dismisses the
  wait and advances, same as it always has for any step.
- Halt (tested separately) behaves exactly as before — untouched by this work.
- Beep now actually sounds alongside the voice line on every nag, after fixing
  the `playTone()` gap above.
- Default content "Did you finish {step_name}?" speaks with the real step name
  substituted; overriding it via the "Talking Content" item and typing/inserting
  different text works, after the `content`/`content2` fix above.

Merged into `personal` via `git merge --no-ff feat/halt-nag-interval`.

## 3. Day-of-week condition on a step

Branch: `feat/day-of-week-condition`

**What:** a step only runs on the days you select (Mon–Sun multiselect); other
days it's skipped entirely. Steps only — **deliberately not on groups** (see
below). The timer list also shows, per timer, today's actual duration plus a
min–max range for how much a day condition could ever shrink or grow it.

**Design:**
- `StepEntity.Step` gets `val conditionDays: List<Boolean>? = null` —
  Monday-based, same convention as `SchedulerEntity.days`. `null` means no
  condition (always runs); a list with every day checked is *also*
  unconditioned in effect (never excludes a day) — both cases matter and are
  handled identically everywhere, including in the min/max math below.
- **No condition on `Group`.** Originally built (a group's own
  `conditionDays`, gating the whole group), then deliberately ripped back out:
  a group condition combined with per-step conditions inside it needs
  AND-of-both-match semantics to make sense, which is confusing to reason
  about for a feature that isn't used with groups anyway. Simpler to keep
  conditions on steps only and not support them on groups at all.
- Since steps round-trip as a single JSON blob (`StepConverters` in
  `data/.../db/Converters.kt`), adding the field needed no Room migration —
  just `StepData.kt` + `StepMapper.kt`/`StepOnlyMapper`, Moshi defaults it to
  `null` for any timer saved before this landed.
- `TimerMachineHelper.kt`'s existing `shouldSkip` hook (already used by
  `getTotalTime()`/`getTimeBeforeIndex()`) now also checks `conditionDays`
  against a day-of-week index, threaded as a `calendarDayIndex: Int`
  parameter (defaulting to today) through `shouldSkip`/`getTotalTime`/
  `getTimeBeforeIndex`, so tests can pass a fixed day instead of depending on
  whatever day the test happens to run.
- **`TimerMachine.provideFirstTask()` didn't consult `shouldSkip` at all** —
  found while making sure a conditioned-off *first* step actually gets
  skipped. Turns out this was already a latent gap shared with the existing
  `SKIP` behavior's `Target.First` option (a step skipped "on the first loop"
  would still run once at the very start of a fresh timer). Fixed by giving
  `provideFirstTask()` the same skip-forward loop `provideNextTask()` already
  had — a correctness fix to already-shipped `SKIP` behavior, not just new
  code for this feature.
- **`getMinTotalTime()`/`getMaxTotalTime()`: the real bug of this feature.**
  First cut used a shortcut — "max" = pretend every condition always matches,
  "min" = pretend every condition (that excludes ≥1 day) never matches. Wrong:
  two steps with *complementary* conditions (e.g. one Mon/Wed/Fri, the other
  every other day) each individually "can be skipped," yet together guarantee
  one of them runs every single day — the shortcut's min was 0, the real
  minimum is never 0. Fixed by computing the actual total for each of the 7
  real days (`getTotalTime(calendarDayIndex = 0..6)`) and taking the genuine
  min/max of those 7 numbers — correct by construction, no per-step
  reasoning to get wrong. `TimerMinMaxTotalTimeParameterizedTest.kt` locks
  this down with 6 hand-reasoned cases (complementary conditions, a real gap
  day where neither step runs, overlapping conditions, all-days-checked,
  zero-days-checked, no condition) each checked two ways: against a
  by-hand-computed expected value, and against an independent brute-force
  sweep of the 7 days — so a future regression to the "pretend" shortcut
  fails loudly.
- UI: a calendar icon (`ic_event`) sits in the same column as the existing
  "+" add-step icon — stacked vertically below the colored step dot, above
  "+" — opening `ConditionDaysDialog` (`app-timer-edit/.../media/
  ConditionDaysDialog.kt`, modeled on `SkipDialog.kt`): a 7-day toggle row
  reusing the `MultiSelectToggleGroup`/`CircularToggle` widgets and
  `WeekdaysFormatter` locale-aware day ordering the Scheduler feature already
  uses. "Any day" clears the condition back to `null`. The icon dims when no
  condition is set, lights up (step's type color) once one is, and a compact
  single-letter day code (`WeekdaysFormatter.produceCompactDataString`, a
  fixed unambiguous `M T W H F A U` per day — not locale weekday names, which
  collide on Tue/Thu and Sat/Sun) appears directly under the icon, in the
  same narrow column, only when a condition is set.
- Originally tried starting the chip area (`layoutBehaviour`) further right
  to dodge the new icon; simpler fix was putting the icon in the icon column
  that already existed (shared with "+"), which was never at risk of
  overlapping the behaviour-chip area regardless of chip count.
- Timer list (item 1's duration display) now shows two things per timer: a
  green-bordered hourglass + today's actual duration (`ic_time_panel_remaining`
  tinted green, reusing the running-screen's existing "remaining time" icon —
  shown for *every* timer, conditioned or not) and, only when a condition
  creates real variability (min ≠ max), a blue hourglass + `min - max` range
  next to it. Both use a new compact duration format (`Long.produceCompactTime()`
  in `TimeConverter.kt`) — `"3h10m4s"`, `"1h"`, `"54s"`, no leading zeros or
  colons — scoped to this one display, not the existing colon `produceTime()`
  used elsewhere in the app.
- Scoped to the main step editor (`EditActivity.kt`) only — not
  `UpdateStepDialog.kt`'s quick single-step-edit popup (used from the running
  screen). That dialog *does* need to preserve `conditionDays` when a step is
  edited there, though — found and fixed a real data-loss bug where its "OK"
  handler rebuilt a fresh `StepEntity.Step` without carrying the field
  through, which would have silently wiped a step's day condition the first
  time someone tweaked its length or behaviour from the running screen.

**Touches:** `StepEntity.kt` (new field + `matchesDayOfWeek`/
`todayCalendarDayIndex`, domain) · `StepData.kt` + `StepMapper.kt` (data) ·
`TimerMachineHelper.kt` (`shouldSkip`, day-index threading, `getMinTotalTime`/
`getMaxTotalTime`) · `TimerMachine.kt` (`provideFirstTask` skip-forward fix) ·
new `ConditionDaysDialog.kt` + `dialog_condition_days.xml` ·
`item_edit_step.xml` (icon + day-letters column) · `EditableStep.kt` (field,
click handler, tint, day-letters binding) · `EditActivity.kt` (handler method,
`conditionDays` threaded through every place a `StepEntity.Step` gets built
from or converted to an `EditableStep`) · `UpdateStepDialog.kt` (data-loss fix
above) · `WeekdaysFormatter.kt` (`produceCompactDataString`) ·
`TimeConverter.kt` (`produceCompactTime`) · `TimerViewModel.kt`
(`TimerDuration(today, min, max)`) · `MutableTimerItem.kt` /
`CollapsedViewHolder.kt` / both `list_item_timer_collapsed*.xml` (icon-based
today/range display) · new `ic_event.xml` + `background_timer_duration_today.xml`
· new strings · `app-timer-edit/build.gradle.kts` (added the
`toggleButtonGroup` dependency, already used by `app-scheduler`).

**Tests:** `StepEntityKtTest.kt` (`matchesDayOfWeek`) ·
`TimerMachineHelperKtTest.kt` (day-condition skip cases) ·
`TimerMinMaxTotalTimeParameterizedTest.kt` (6-case parameterized suite for
min/max, see above) · `WeekdaysFormatterTest.kt` (`produceCompactDataString`)
· `TimeConverterTest.kt` (`produceCompactTime`) — all green, plus every
pre-existing test in `domain`/`presentation`/`app-base` still green.

**Effort:** noticeably more than the original 1–2 day estimate — most of the
overrun was the min/max correctness bug (an easy-looking shortcut that was
actually wrong) and several rounds of UI placement feedback (icon overlapping
behaviour chips, day-letters position, "Up to" text → icons, group support
added then removed).

**Status:** done

**Manual test:** Built and iterated live on-device via `scripts/deploy.sh`
(`personal` flavor) against a physical device, screenshots taken after each
change to confirm layout before handing back for review. Confirmed:
setting/clearing a day condition on a step persists through save/reload;
a conditioned-off step is skipped at runtime, including as the very first
step of a timer; the calendar icon and day-letters render correctly
alongside behaviour chips of any count, in both light data (empty step) and
heavy (Confirm/Voice/Beep) cases; the timer list shows the green
today-duration box on every timer and the blue min-max range only when a
step's condition creates real variability; verified against a two-step
timer with complementary Mon/Wed/Fri vs. rest-of-week conditions (the case
that broke the first min/max implementation) that min and max now compute
correctly instead of showing a bogus 0. Merged into `personal` via
`git merge --no-ff feat/day-of-week-condition`.

## 4. Time-of-day range condition

Branch: `feat/time-range-condition`

**What:** a step is active only inside — or only outside — a configured
start–end time range. Handle overnight ranges (22:00–06:00) as a real case.

**Touches:** same condition field and `shouldSkip` hook as item 3 · time-range
picker UI.

**Effort:** 1 day.

**Status:** not started

**Manual test:** _(fill in after building)_

## 5. QR-scan dismiss mode for HALT

Branch: `feat/halt-qr-dismiss`

**What:** a new dismiss mode on `HALT` — instead of tapping to resume, scan a QR
code (any code, or one specific saved code) to clear the step.

**Touches:** same behavior/adapter/UI files as item 2, plus a new full-screen scan
screen · `CAMERA` permission in the manifest · an on-device barcode-scanning
library (ML Kit barcode scanning avoids a network dependency, unlike some
alternatives).

**Effort:** 2–4 days. Last on purpose — biggest of the five, new permission.

**Status:** not started

**Manual test:** _(fill in after building)_

## 6. Searchable step-level activity log

Branch: `feat/step-activity-log`

**What:** answer "when was the last time I did X?" (e.g. "took my medication") by
recording every individual step completion — not just whole-timer runs — with a
timestamp (local date + time), the timer name, the step name, and how it ended
(auto-advanced when its countdown hit zero, or manually dismissed). Searchable by
timer name or step text, sorted newest-first, so the answer is a one-line search
away instead of scrolling a calendar.

**What already exists (the "rudiment"):** `TimerStampEntity` (id, timerId, start,
end — `domain/.../entities/TimerStampEntity.kt`) plus `TimerStampRepository` and
`AddTimerStamp`, recorded once per full timer run in
`MachinePresenter.end():591` using that run's begin time and "now". `GetRecords.kt`
turns these into the existing Record screens (`app-timer-list/.../record/`): an
overview pie chart, a timeline bar chart, and a calendar heatmap with a day list.
This is timer-run granularity only — it can say "you ran the Meds timer 3 times
last Tuesday" but not "you confirmed the Ibuprofen step at 2:14 PM" — and it has
no text search and no record of how a step ended.

**Design:**
- New `StepStampEntity(id, timerId, timerName, stepName, timestamp, confirmMethod)`
  where `confirmMethod` is `AUTO` (countdown reached zero) or `MANUAL` (dismissed
  by hand today; item 5 will add QR as a second manual variant once it exists —
  worth widening this to `AUTO` / `TAP` / `QR` at that point rather than a single
  `MANUAL` bucket). `timerName`/`stepName` are stored as plain-text snapshots, not
  foreign keys — `StepEntity.Step` (`domain/.../entities/StepEntity.kt:8`) has no
  stable id, only a `label`, and timers/steps can be renamed or deleted later. A
  snapshot keeps old log entries readable and searchable regardless of later edits,
  and it's also exactly what full-text search needs to match against.
- Hook point: `TimerMachine.kt`'s per-step task already distinguishes the two
  completion paths structurally — `CountDownTimerTask.onFinish()`
  (`.../stream/task/CountDownTimerTask.kt:38`, fires when the countdown reaches
  zero — this is "auto") vs `StopwatchTask.onFinish()`
  (`.../stream/task/StopwatchTask.kt:46`, fires when a HALT step is stopped by an
  explicit action — this is "manual"). That's the natural place to record a step
  stamp with its confirm method, alongside (not instead of) the existing
  timer-level stamp already recorded in `MachinePresenter.end()`.
- New Room table + DAO (`StepStampDao`): insert, plus a search query —
  `SELECT * FROM StepStamp WHERE timerName LIKE '%' || :query || '%' OR stepName
  LIKE '%' || :query || '%' ORDER BY timestamp DESC`.
- New searchable log screen: a single search box (same pattern as item 7's timer
  search) over a newest-first list, each row showing date + time, timer name, step
  name, and how it ended (e.g. an "auto" vs "dismissed" label or icon).
- No retention/pruning policy for now — personal-scale volume (a handful of steps
  a day) makes this a non-issue; revisit only if storage or list performance ever
  becomes noticeable.

**Touches:** `StepStampEntity.kt` (domain) · `StepStampRepository` interface + impl
(data) · new Room entity/DAO + migration · `AddStepStamp` use case ·
`SearchStepStamps` use case · new ViewModel · new screen (list + search box) ·
wire the recording call into `TimerMachine.kt`'s per-task completion (or
`MachinePresenter.kt` at the same point each task finishes) so it fires once per
step, not just once per full run.

**Effort:** 2–3 days — a new table/migration, one use-case pair, and one new
searchable list screen, building on the existing stamp/record pattern already in
the codebase; smaller than composable timers or the soundtrack feature.

**Status:** not started

**Manual test:** _(fill in after building)_

## 7. Search timers by name

Branch: `feat/timer-search`

**What:** a search field in the timer list toolbar, live-filtering by name.
**Global** — searches every timer in every folder regardless of which folder
you're currently viewing, not just the current folder's contents.

**Touches:** new DAO query (`SELECT id, name, folderId FROM TimerItem WHERE name
LIKE '%' || :query || '%'`, folder-independent) · repository/use-case wrapper ·
`TimerViewModel` (search query state, switches the list source between
per-folder browsing and the global search results while a query is active) ·
toolbar `SearchView` in `TimerFragment`.

**Effort:** 0.5–1 day.

**Status:** not started

**Manual test:** _(fill in after building)_

## 8. Import a timer from a JSON file (including Google Drive)

Branch: `feat/timer-json-import`

**What:** pick a `.json` file — Google Drive included, since it's just a
standard Android file picker — parse it as a timer, then choose which folder
to save it into as a new timer.

**Already exists and gets reused as-is:** the editor's "Copy to clipboard" /
"Create from clipboard" actions (`EditActivity.kt:230-246`) already export and
import a single timer as real Moshi JSON (`AppDataRepositoryImpl.kt:22-39`),
via `ShareTimer.shareAsString` / `receiveFromString`. This is genuinely most of
the feature already built — what's missing is a file source and a folder
destination instead of clipboard-only import into the currently-open editor.

**What's new:** `ActivityResultContracts.OpenDocument()` file picker (Drive
shows up automatically as a document provider, no separate Drive API
integration) → read the file's text → `ShareTimer.receiveFromString(...)` →
folder picker (reuse the folder-selection piece already built for
`TimerPicker.kt`) → `AddTimer` with the chosen `folderId`, as a new entry point
from the timer list rather than from inside the editor.

**Also do:** once this exists, export one real timer and save the resulting
JSON into this plan file as a documented example — that's what makes it easy
to point an AI at later ("generate JSON matching this shape").

**Effort:** 0.5–1 day — small, because the export/import round trip already
works.

**Status:** not started

**Manual test:** _(fill in after building)_

## 9. Composable timers — run a saved timer as a step, N times

Branch: `feat/composable-timer-step`

**What:** a new step type that says "run saved timer X, N times" as one step
among others in a timer — not the existing `Group` loop (which only loops
steps physically copy-pasted into that one timer), and not the existing
"trigger next timer" pointer (`TimerMoreEntity.triggerTimerId`, which starts
the other timer as a brand-new separate run only once this one fully ends —
checked in `MachinePresenter.kt:606-611`, confirmed it's a hand-off, not an
inline splice). This is a genuine reference: edit "Pushups" once, and every
timer that runs it N times picks up the change next run.

**Design:** a new `StepEntity.TimerReference(timerId: Int, loop: Int)` sealed
subtype, resolved at the point a timer is loaded to run (and when computing
duration) by loading the referenced `TimerEntity` and expanding its steps
inline as if it were a `Group` with that loop count — so all the existing
loop/skip/duration/index machinery (including item 1's duration display and
items 3/4's conditions) keeps working unchanged. The editor shows it as a
collapsed reference chip, picked via the `TimerPicker` dialog that already
exists and is already used for `triggerTimerId` selection
(`EditActivityMoreDialog.kt:69`) — no new picker UI to build.

**Cycle detection:** reject saving a reference that would let a timer
(transitively) reference itself.

**Deleting or renaming a referenced timer:** renaming/editing is always
allowed and takes effect everywhere it's referenced, next run. Deleting a
timer that's referenced elsewhere shows a dialog with two choices —
"Delete and remove it from every timer that references it" (double-confirmed)
or "Cancel" — never a silent cascade, never a silent block.

**Touches:** `StepEntity.kt` (new sealed subtype) · step JSON adapter / DB
converters (new serialization case) · `TimerMachineHelper.kt` (reference
resolution before existing navigation/duration/skip logic runs) · editor step
list UI (new step type, reusing `TimerPicker`) · `DeleteTimer` use case (new
"which timers reference this one" query + the two-choice dialog).

**Effort:** 3–5 days — heaviest of this batch, last on purpose.

**Status:** not started

**Manual test:** _(fill in after building)_

## 10. Local music playlist — searchable, persists across steps, layers with alerts

Branch: `feat/timer-soundtrack`

**What:** a "Soundtrack" attached to the whole timer, not any one step — plays
a playlist you build once (shuffled or in order), looping for as long as the
timer runs, continuing across every step boundary until the timer ends,
instead of stopping when the step that started it advances. Layers with
BEEP/VOICE by manually ducking the soundtrack's own volume while an alert
plays, rather than fighting over exclusive OS audio focus — this is all your
own app's audio, so there's no need for the cross-app ducking dance.

**Playlist builder — corrected from the original spec:** a single search
textbox, live-filtered against Android's `MediaStore.Audio.Media` (title,
artist, album, filename columns), not a folder or file picker. Requires
`READ_MEDIA_AUDIO` (Android 13+) / `READ_EXTERNAL_STORAGE` on older versions.
Reuses the same search-box pattern as item 7, applied to a different list.
Confirmed use case: your own downloaded local files, not a streaming
service's DRM'd downloads — those aren't visible to `MediaStore` at all, so
this design only works because the files are genuinely yours on-device.

**Cross-device portability — fuzzy match, not exact:** a `MediaStore` row ID
or content URI is specific to one device's database and won't resolve on a
different phone (or after a rescan on the same one). So each saved playlist
track stores its metadata (title, artist, album, filename), not just a URI.
At playback time: try the last-known URI first (cheap, works when nothing's
changed); if that fails to resolve, fall back to a fuzzy match against that
device's `MediaStore` — normalize both sides (lowercase, strip punctuation/
diacritics/noise like "(remastered)" or "feat. X"), score similarity per
field with a small hand-rolled string-distance function (no external library
needed at this library size), weighted title > artist > album/filename, and
accept the best candidate only above a confidence threshold. Below that
threshold the track is flagged unresolved rather than guessing — the rest of
the playlist keeps playing, and the unresolved track surfaces somewhere you
can manually reselect it on that device.

**Touches:** new runtime permission · `MediaStore` query + search UI · new
playlist entity/DB table · fuzzy-match utility (normalize + weighted
similarity scoring) · a player component with its own lifecycle independent
of step advancement · manual-duck logic layered onto the existing audio-focus/
beep/TTS path.

**Effort:** 4–6 days — fuzzy re-matching adds a bit over the original
estimate, real on-device audio testing still required (OEM quirks are common
here) — the biggest single item in this batch.

**Status:** not started

**Manual test:** _(fill in after building)_

## 11. Proactive, offline-capable TTS pre-baking

Branch: `feat/tts-proactive-bake`

**What:** this app already has a real TTS disk-cache feature — "Text-to-speech
bakery" (Settings toggle `pref_is_tts_bakery_open`) — that renders any spoken
phrase to a file via the on-device engine's `synthesizeToFile` and reuses it
next time (`TtsBakery.kt`, `TtsBakeryWorker.kt`). Two gaps stand between that
and "guaranteed offline": it only bakes a phrase *after* you've heard it live
once (`TtsSpeaker.kt:334-336`), and the background baking job requires network
(`Constraints(requiredNetworkType = NetworkType.CONNECTED)` in
`TtsBakery.kt:21-27`) even though the render itself is entirely local.

**Change:** when a timer is saved, walk every `VOICE` behavior's text across
all its steps and schedule baking for each one immediately, instead of
waiting for a live run to trigger it. Drop the baking job's network
requirement (`NetworkType.NOT_REQUIRED`) since `synthesizeToFile` doesn't
actually call out anywhere. Net effect: save a timer once (while the app is
running, so WorkManager can execute), and every phrase in it is a cached local
file before you ever hit "start" — nothing left to synthesize on the fly,
signal or no signal. Still depends on the OS-level offline voice pack being
installed for your language (Settings → System → Languages & input →
Text-to-speech output → engine settings) — that's outside this app's control,
worth confirming once on your device regardless of what we build here.

**Touches:** `SaveTimer.kt` / `AddTimer.kt` (walk steps, collect distinct
`VOICE` text, call `TtsBakery.scheduleBaking` per phrase) · `TtsBakery.kt`
(constraint change).

**Effort:** well under a day — wiring on top of an existing, working system.

**Status:** not started

**Manual test:** _(fill in after building)_

## 12. Self-hosted high-quality TTS voice, chosen per timer

Branch: `feat/tts-self-hosted-voice`

**What:** swap the on-device Android TTS engine (the "shitty" one) for a
self-hosted neural voice running on your Mac, reachable over local WiFi. No
cloud account, no API key, no ongoing cost, ever — chosen over a cloud
provider (Azure/Google both have generous free tiers, researched and
documented below for reference if this ever changes) specifically to avoid
depending on an external account and to keep everything on your own network.

**Voice is per-timer, not a single app-wide setting.** Different timers want
different voices — Nicole for meditation, Bella as the default "normal"
voice. Each timer gets its own voice choice, picked from a preview list.

**Voice picker with a baked-in sample:** a new picker (reachable from the
same "more" settings screen as the existing `triggerTimerId` picker in
`EditActivityMoreDialog.kt`) lists the available voices, each with a play
button that speaks one fixed sample line so you can actually judge the voice
before committing — not generic demo text, this specific line: *"Make sure
this voice is what you really want to listen to all day, or otherwise you
will get annoyed at it - and quickly."* Since the sample text is always the
same, every voice's sample can be pre-baked once (first time the picker opens)
through the same bakery pipeline as everything else, so previews play back
instantly rather than waiting on a fresh render each time you tap one.

**Why this is a small, contained change:** item 11 already moved this app to
"bake once at save time, always play from a cached file." The bake step
(`TtsBakeryWorker.synthesize()`) currently calls the on-device
`TextToSpeech.synthesizeToFile(...)`; playback (`TtsSpeaker.kt`) already just
plays whatever audio file is in the cache via `RingtonePreviewKlaxon`,
regardless of where that file came from. So this is really just: change what
produces the cached file, from a local API call to a local-network HTTP call.
Nothing about caching or playback needs to change.

**Server:** [Kokoro-82M](https://github.com/hwdsl2/docker-kokoro) — Apache
2.0 (no usage restrictions), runs fine on CPU, 54 voices across 8 languages,
faster than real-time, and this Docker image already exposes an
OpenAI-compatible `/v1/audio/speech` HTTP endpoint with no signup or key
needed. One `docker run` on your Mac. Worth an ear-test against
[Chatterbox](https://github.com/resemble-ai/chatterbox) (MIT license) too —
reportedly preferred over ElevenLabs in Resemble's own blind listening test —
though its Docker/API packaging wasn't confirmed during research the way
Kokoro's was, so start with Kokoro and treat Chatterbox as a possible swap
once the plumbing exists, not a blocker.

**App changes:**
- `TtsBakeryWorker.synthesize()`: replace the on-device
  `synthesizeToFile` call with an HTTP POST to the configured server's
  `/v1/audio/speech` endpoint (voice name included in the request), save the
  returned audio to the disk cache.
- **Cache key must become `(voice, text)`, not just `text`.** Today's cache
  (`TtsBakeryDiskCache`) is keyed on phrase text alone, which was fine with
  one global voice — with per-timer voice, "3... 2... 1..." spoken by Nicole
  and by Bella need to be two different cache entries, not a collision. Every
  call site that bakes or looks up a phrase (`TtsSpeaker.speak`,
  `TtsBakery.scheduleBaking`/`getSpeechFile`, `TtsBakeryWorker`) needs the
  voice threaded through, not just the text.
- **New per-timer field:** `TimerMoreEntity.ttsVoice: String? = null` (null =
  fall back to a single app-wide default voice setting, so timers created
  before this feature keep working unchanged). Adding a `more` field means
  touching the same handful of places the existing code comment already
  flags for it: `TestData`, `TimerMoreMapper`, `MappersTest`, `OneFragment`,
  `EditActivity` — plus the step/timer JSON adapter, so item 8's JSON
  import/export round-trips the chosen voice too.
- **Run-engine plumbing:** wherever a running timer currently triggers
  `TtsSpeaker.speak(...)` for a `VOICE` behavior (`TimerMachine.kt` /
  `MachinePresenter.kt`), it needs to read the current timer's `ttsVoice` and
  pass it along, not assume a single global voice.
- New setting: server address (`http://<mac-local-ip>:8880`, from the Kokoro
  image's default port), with a "test connection" action, plus the app-wide
  default voice used when a timer hasn't picked one. Local IPs can drift on
  DHCP — worth reserving a static IP for the Mac in your router settings so
  this doesn't need re-entering.
- Baking job's network constraint goes back to `NetworkType.CONNECTED` (item
  11 dropped it because on-device synthesis needed no network; this path
  does). If your Mac is off or you're off home WiFi when saving a timer, the
  HTTP call just fails and WorkManager's existing retry-with-backoff
  (`Result.retry()` in `TtsBakeryWorker.kt:64`) picks it up next time
  something connects — no new retry logic needed.
- **Baking itself needs a fallback chain, not just a live-speak fallback.**
  `TtsBakeryWorker.synthesize()`: try the self-hosted Kokoro call first; if
  it's unreachable (Mac off, off home WiFi), bake via the on-device engine
  instead — still proactively, still producing a cached file at save time,
  never leaving a phrase to be synthesized live at alarm time. The on-device
  fallback can't reproduce a specific Kokoro voice (Nicole/Bella don't exist
  on-device) — it uses the phone's own default voice/engine, so this trades
  voice identity for reliability, not a bug.
- **Fallback-baked entries need to self-upgrade.** Tag any cache entry baked
  via the on-device fallback (e.g. a `source` field alongside the cached
  file: `kokoro` vs `on-device`). Whenever that timer is next saved/edited —
  and/or on a periodic sweep — re-attempt Kokoro for any `on-device`-tagged
  entries and replace them in place if it succeeds. Without this, a timer
  silently stays on the lesser voice forever after one save with the Mac
  off, with no obvious sign anything's wrong.
- **Already-existing safety net, no work needed:** if a phrase is ever
  actually spoken before it's been baked at all (edge case — brand new
  content encountered mid-run), `TtsSpeaker.kt` already falls back to the
  on-device engine live rather than staying silent. This is the genuinely
  last-resort case; the two bullets above mean it should rarely trigger.

**Effort:** 4–6 days for the app-side change — bumped up from the original
single-global-voice estimate, since per-timer voice touches the cache key,
the run engine, and a new picker UI, and the fallback chain needs a
self-upgrade path, not just a one-shot bake step. Separately, get comfortable
running the Docker container and picking voices you like before writing any
code — that part is pure listening, not engineering.

**Status:** not started

**Manual test:** _(fill in after building)_

### Cloud options, for reference (not being used, self-hosting was chosen)

Researched in case self-hosting ever stops being worth it. Every provider's
free tier is enormous relative to actual personal alarm-phrase volume (short
text, each phrase synthesized once thanks to caching), so "free" here means
genuinely free at this scale, not a teaser tier:

| Provider | Free tier | Then | Notes |
|---|---|---|---|
| [Azure Neural TTS](https://texttolab.com/blog/azure-text-to-speech-pricing) | 500K chars/mo, **doesn't expire** | $16/1M | Best "free forever" option if cloud is ever wanted |
| [Google Chirp 3: HD](https://docs.cloud.google.com/text-to-speech/docs/list-voices-and-types) | 1M chars/mo | $30/1M | Newer/broader than the "Journey" voices already tried |
| [Amazon Polly Neural](https://texttolab.com/blog/amazon-polly-pricing) | 1M chars/mo, **first 12 months only** | $16/1M | Free tier expires |
| [ElevenLabs](https://texttolab.com/pricing) | 10K chars/mo | $5/mo for 30K | Free tier too small to be useful here |
| [OpenAI TTS](https://texttolab.com/blog/openai-tts-pricing) | none (one-time $5 signup credit) | $15–30/1M | No ongoing free tier |

All of these need a cloud account and API key even to use the free tier.

## 13. Sound sequencing across behaviors on one step

Branch: `feat/behavior-sound-sequencing`

**What:** noticed while building item 2 — when a step has more than one
sound-producing behavior (e.g. Beep + Music + Voice all on the same step), they
currently fire independently and talk over each other instead of playing in a
defined order with gaps between them. Add a way to define, per step, the
sequence sound behaviors play in and the delay between each (e.g. Beep, wait
0.5s, Music, wait 0.5s, Voice).

**Nice-to-have, last on purpose** — deliberately not designed in detail yet.
Revisit once the higher-priority items above are done.

**Status:** not started

**Manual test:** _(fill in after building)_

---

## Build & install (Mac → Android, USB)

**Quick path: `scripts/deploy.sh`** — builds `installPersonalDebug` for
whatever branch is currently checked out and launches it on the connected
device, in one command. Always the `personal` flavor specifically (distinct
name/icon from item 0), never `dog`/`google`/`other` — those share the stock
"TimeR Machine" name and icon with each other and with the Play Store install,
which makes them impossible to tell apart in the app drawer or in search.
`personal` doesn't have that problem and is what every feature branch should
be built and tested against, per the workflow above.

One-time setup, done once per Mac/phone pair:
1. Enable Developer Options and USB debugging on the phone (Settings → About
   phone → tap Build number 7×, then Settings → Developer options → USB
   debugging).
2. Connect the phone to the Mac via USB, accept the "Allow USB debugging?"
   prompt on the phone.
3. From the repo root: `adb devices` — confirm the phone shows as `device`, not
   `unauthorized`.

Every deploy after that: `scripts/deploy.sh`.

Manual equivalent, if ever needed instead of the script:
`./gradlew installPersonalDebug` (or `./gradlew assemblePersonalDebug` then
`adb install -r app/build/outputs/apk/personal/debug/app-personal-debug.apk`
for the APK file itself), then launch it on the phone and confirm the
name/icon from item 0 show up.

## Licensing note

This fork stays GPLv3. Running your own modified build privately on your own
device, without distributing it, carries no obligation to publish source — that
obligation only applies if you convey (distribute/sell) the build to someone else.
