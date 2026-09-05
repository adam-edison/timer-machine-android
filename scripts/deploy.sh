#!/usr/bin/env bash
# Build and install the personal-flavor debug build onto one of the two test
# phones (over USB or wireless debugging — see wireless-debugging.md), then
# launch it. Always deploys whatever branch is currently checked out.
#
# Usage: scripts/deploy.sh

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

ADB="$HOME/Library/Android/sdk/platform-tools/adb"
if [ ! -x "$ADB" ]; then
  ADB="adb"
fi

PACKAGE="io.github.deweyreed.timer.personal"
MAIN_ACTIVITY="io.github.deweyreed.timer.ui.MainActivity"

OLD_MODEL="SM_G975U"   # Android 12 phone (Galaxy S10+)
NEW_MODEL="SM_S901U1"  # Android 16 phone (Galaxy S22 Ultra)

find_serial() {
  local model="$1"
  "$ADB" devices -l | awk -v needle="model:$model" '$0 ~ needle { print $1; exit }'
}

echo "Which device do you want to deploy to?"
echo "  1) Old device (Android 12)"
echo "  2) New device (Android 16)"
read -rp "Enter 1 or 2: " choice

case "$choice" in
  1) target_model="$OLD_MODEL"; label="old (Android 12)" ;;
  2) target_model="$NEW_MODEL"; label="new (Android 16)" ;;
  *)
    echo "Invalid choice: $choice" >&2
    exit 1
    ;;
esac

serial="$(find_serial "$target_model")"
if [ -z "$serial" ]; then
  echo "No connected device found for the $label phone (model $target_model)." >&2
  echo "Check it's paired/connected — see wireless-debugging.md — then check: $ADB devices -l" >&2
  exit 1
fi

echo "Deploying to $label device ($serial)"

ANDROID_SERIAL="$serial" ./gradlew installPersonalDebug

"$ADB" -s "$serial" shell am start -n "$PACKAGE/$MAIN_ACTIVITY"
