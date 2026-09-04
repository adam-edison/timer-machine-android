#!/usr/bin/env bash
# Build and install the personal-flavor debug build onto a USB-connected device,
# then launch it. Always deploys whatever branch is currently checked out.
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

./gradlew installPersonalDebug

"$ADB" shell am start -n "$PACKAGE/$MAIN_ACTIVITY"
