# Sourced by deploy.sh and clean-deploy.sh — resolves which of the two known
# test phones to target. Sets $ADB, $PACKAGE, $MAIN_ACTIVITY, $serial, $label.
#
# Pass a device choice (1 or 2) as $1 to skip the interactive prompt.

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

choice="${1:-}"
if [ -z "$choice" ]; then
  echo "Which device do you want to target?"
  echo "  1) Old device (Android 12)"
  echo "  2) New device (Android 16)"
  read -rp "Enter 1 or 2: " choice
fi

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
