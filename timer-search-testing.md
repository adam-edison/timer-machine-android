# Testing item 7 — search timers by name

Branch: `feat/timer-search`. Nothing is committed yet — this all needs to
pass before the feature code gets committed and merged into `personal`.

## What changed from the original plan

The plan text called for a toolbar `SearchView` inline on the main timer
list. Instead, both **Search Timers** and **Step Log** (item 6) moved into
the left drawer as their own top-level screens — you asked for this mid-build,
reasoning that both are global (search across every folder; step log across
every timer) and didn't belong cluttering the main list or its overflow menu.
Step Log's entry point moved from the timer list's overflow menu (⋮) to the
drawer; its screen itself is unchanged.

Tapping a search result opens that timer's running screen directly (same as
tapping a timer in the main list) — the search screen itself is a simple
read-only list (name + which folder it's in), not the full start/pause/edit
timer card.

## 0. Before you start

Both phones should be connected — check:
```
adb devices -l
```
Should show exactly one entry per phone (`192.168.1.x:xxxx`, `device` state).
If a phone is missing or shows twice, see `wireless-debugging.md`.

There's an unsaved "Ibuprofen Reminder" draft sitting open on the Create
Timer screen on the Android 16 phone from a build-verification pass — either
save it (handy as one of your test timers, see below) or discard it before
starting.

## 1. Deploy and manually test on each phone

```
scripts/deploy.sh
```
Pick 1 (old/Android 12) or 2 (new/Android 16). Repeat everything below on
the other phone once the first one checks out.

**a. Drawer entries** — open the left drawer (hamburger icon, top-left).
Confirm **Search Timers** (magnifying-glass icon) and **Step Log** (history
icon) both appear directly under **Timers**, above **Schedulers**. Confirm
Step Log no longer appears in the timer list's overflow menu (⋮) — only
**Records** should be there now.

**b. Create a few test timers** — in the main **Timers** list, create at
least two timers in the default folder with distinct, greppable names (e.g.
"Ibuprofen Reminder", "Morning Stretch"), and one more in a *different*
folder (create a new folder via the folder dropdown, or move an existing
timer to trash to check exclusion — see (e) below).

**c. Global search across folders** — open **Search Timers** from the
drawer. With the search box empty, confirm every timer you created shows up
(regardless of which folder it's in), each with its folder name shown
underneath. Type part of a name (e.g. "ibu") and confirm the list live-filters
to matching timers only; clearing the box shows everything again.

**d. Tap to open** — tap a search result. Confirm it opens that timer's
running screen (same as tapping it from the main **Timers** list), not the
editor.

**e. Trash is excluded** — move one of your test timers to the trash (long
press → delete, from the main list). Confirm it stops appearing in Search
Timers results, even when the search box is empty or matches its name
exactly.

**f. Drawer swipe-lock** — confirm the drawer can be opened by swiping from
the left edge while on the Search Timers or Step Log screens (same as on
Timers/Schedulers/Settings), and that both screens show a back arrow
(top-left) rather than the hamburger icon — tapping it returns to the
previous screen.

## 2. Report back

Once both phones check out on every item above, let Claude know — the
"Manual test" section in `PERSONAL_PLAN.md` gets filled in for real, the
feature code gets committed, and `feat/timer-search` merges into `personal`.
If anything fails, report what you saw instead of what's expected and it
gets fixed before merging.
