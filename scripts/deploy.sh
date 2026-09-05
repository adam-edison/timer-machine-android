#!/usr/bin/env bash
# Build and install the personal-flavor debug build onto one of the two test
# phones (over USB or wireless debugging — see wireless-debugging.md), then
# launch it. Always deploys whatever branch is currently checked out.
#
# Keeps the phone's existing app data (timers, preferences, the whole local
# database) — this is the everyday "just update the app" path. Use
# scripts/clean-deploy.sh instead when you need a fresh install with no
# leftover data (e.g. ruling out stale local state while debugging).
#
# Usage: scripts/deploy.sh [1|2]   (1 = old/Android 12, 2 = new/Android 16;
# omit either to be prompted)

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

# shellcheck source=scripts/lib/select-device.sh
source "$REPO_ROOT/scripts/lib/select-device.sh" "${1:-}"

echo "Deploying to $label device ($serial)"

ANDROID_SERIAL="$serial" ./gradlew installPersonalDebug

"$ADB" -s "$serial" shell am start -n "$PACKAGE/$MAIN_ACTIVITY"
