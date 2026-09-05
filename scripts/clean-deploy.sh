#!/usr/bin/env bash
# Same as scripts/deploy.sh, but uninstalls the app first — a full wipe of
# its local data (Room database, preferences, everything) before installing
# completely fresh. Use this when you specifically need a clean-slate test
# (e.g. ruling out stale local state as the cause of a bug). Use plain
# deploy.sh for everyday updates — that one keeps your existing timers/data.
#
# Usage: scripts/clean-deploy.sh [1|2]   (1 = old/Android 12, 2 = new/Android
# 16; omit either to be prompted)

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

# shellcheck source=scripts/lib/select-device.sh
source "$REPO_ROOT/scripts/lib/select-device.sh" "${1:-}"

echo "Clean-deploying to $label device ($serial) — wiping existing app data first"

"$ADB" -s "$serial" uninstall "$PACKAGE" || true

ANDROID_SERIAL="$serial" ./gradlew installPersonalDebug

"$ADB" -s "$serial" shell am start -n "$PACKAGE/$MAIN_ACTIVITY"
