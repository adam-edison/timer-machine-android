# Personal fork — quick wins plan

Working branch: `personal`, cut from `develop` (this fork's main). Not intended for
upstream contribution. Stays a private, GPLv3-compliant modified version for personal
use only (see licensing note at the bottom).

## Status

- [ ] [0. Personal branding](#0-personal-branding-do-first)
- [ ] [1. Total timer duration shown in the list](#1-total-timer-duration-shown-in-the-list)
- [ ] [2. Nagging reminder interval on HALT](#2-nagging-reminder-interval-on-halt)
- [ ] [3. Day-of-week condition](#3-day-of-week-condition-on-a-step-or-group)
- [ ] [4. Time-of-day range condition](#4-time-of-day-range-condition)
- [ ] [5. QR-scan dismiss mode for HALT](#5-qr-scan-dismiss-mode-for-halt)
- [ ] [6. Search timers by name](#6-search-timers-by-name)
- [ ] [7. Import a timer from a JSON file](#7-import-a-timer-from-a-json-file-including-google-drive)
- [ ] [8. Composable timers](#8-composable-timers--run-a-saved-timer-as-a-step-n-times)
- [ ] [9. Local music playlist](#9-local-music-playlist--searchable-persists-across-steps-layers-with-alerts)

Check a box and flip its section's `Status:` line to `done` in the same commit
that merges the feature branch into `personal`.

## Workflow

- Each feature below gets its own branch, cut from `develop`: `feat/<name>`.
- Build and manually test it, then merge into `personal` with `git merge --no-ff feat/<name>`.
- Commits within a feature branch follow [Conventional Commits](https://www.conventionalcommits.org/):
  `feat(scope): summary`, `fix(scope): summary`, etc.
- Cutting from `develop` (not from `personal`) keeps each feature branch easy to diff
  or rebase against upstream later, and keeps `git pull` / merges from the official
  repo's `develop` into `personal` conflict-free from unrelated work.
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

**Status:** not started

**Manual test:** _(fill in after building)_

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

**Effort:** half a day to a day.

**Status:** not started

**Manual test:** _(fill in after building)_

## 2. Nagging reminder interval on HALT

Branch: `feat/halt-nag-interval`

**What:** add a repeat interval to the existing `HALT` behavior — while a step is
halted, replay its TTS/beep every N seconds instead of once, until dismissed.

**Touches:** `BehaviourEntity.kt` (new field) · `MachinePresenter.kt` /
`TimerMachine.kt` (halt loop) · `BehaviourSettingsView.kt` (interval input) ·
`BehaviourDataJsonAdapter.kt` (serialization).

**Watch for:** the codebase flags, in a comment, four places that need updating for
any new behavior field — find it before writing this one, since it applies to
every feature below that touches a behavior too.

**Effort:** 0.5–1 day.

**Status:** not started

**Manual test:** _(fill in after building)_

## 3. Day-of-week condition on a step or group

Branch: `feat/day-of-week-condition`

**What:** a step or group only runs on the days you select (Mon–Sun multiselect);
other days it's skipped entirely.

**Touches:** new condition field on `StepEntity` · evaluated in `TimerMachine.kt` /
`TimerMachineHelper.kt`'s existing `shouldSkip` hook (already used by
`getTotalTime()`, so a correctly-implemented skip here is automatically reflected
in item 1's duration display) · new day-picker UI.

**Effort:** 1–2 days.

**Status:** not started

**Manual test:** _(fill in after building)_

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

## 6. Search timers by name

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

## 7. Import a timer from a JSON file (including Google Drive)

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

## 8. Composable timers — run a saved timer as a step, N times

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

## 9. Local music playlist — searchable, persists across steps, layers with alerts

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
Reuses the same search-box pattern as item 6, applied to a different list.
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

---

## Build & install (Mac → Android, USB)

Applies to every feature above once its flavor/variant name is confirmed at
build 0. Illustrative task names below assume flavor `personal`, build type
`debug` — Android Gradle Plugin names tasks `install<Flavor><BuildType>`, so
confirm the exact name once flavor 0 lands (`./gradlew tasks --group install`
lists them).

1. Enable Developer Options and USB debugging on the phone (Settings → About
   phone → tap Build number 7×, then Settings → Developer options → USB
   debugging).
2. Connect the phone to the Mac via USB, accept the "Allow USB debugging?"
   prompt on the phone.
3. From the repo root: `adb devices` — confirm the phone shows as `device`, not
   `unauthorized`.
4. `./gradlew installPersonalDebug` — builds and installs directly over USB in one
   step.
   - Alternative if you want the APK file itself:
     `./gradlew assemblePersonalDebug`, then
     `adb install -r app/build/outputs/apk/personal/debug/app-personal-debug.apk`.
5. Launch the app on the phone and confirm the name/icon from item 0 show up, so
   you can tell at a glance it's this build and not the Play Store version.

## Licensing note

This fork stays GPLv3. Running your own modified build privately on your own
device, without distributing it, carries no obligation to publish source — that
obligation only applies if you convey (distribute/sell) the build to someone else.
