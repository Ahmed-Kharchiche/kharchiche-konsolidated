#!/usr/bin/env bash
# CI-only optional signing. Missing secrets produce an unsigned build, never a fake IPA.
set -euo pipefail
: "${RUNNER_TEMP:?This script is intended for an ephemeral GitHub macOS runner}"
keychain="$RUNNER_TEMP/kharchiche-mobile.keychain-db"
profile_record="$RUNNER_TEMP/kharchiche-mobile-profile-path"
if [[ "${1:-prepare}" == cleanup ]]; then
  if [[ -f "$keychain" ]]; then security delete-keychain "$keychain"; fi
  if [[ -f "$profile_record" ]]; then
    profile="$(cat "$profile_record")"
    case "$profile" in "$HOME/Library/MobileDevice/Provisioning Profiles/"*.mobileprovision) rm -f -- "$profile" ;; *) exit 1 ;; esac
  fi
  rm -f -- "$RUNNER_TEMP/kharchiche-mobile.p12" "$RUNNER_TEMP/kharchiche-mobile.mobileprovision" \
    "$RUNNER_TEMP/kharchiche-mobile-profile.plist" "$profile_record"
  exit 0
fi
present=0
for variable in MOBILE_IOS_CERTIFICATE_P12_BASE64 MOBILE_IOS_CERTIFICATE_PASSWORD MOBILE_IOS_PROVISION_PROFILE_BASE64 MOBILE_IOS_SIGNING_IDENTITY; do
  if [[ -n "${!variable:-}" ]]; then present=$((present + 1)); fi
done
if [[ "$present" == 0 ]]; then
  echo 'No Apple signing secrets: building an unsigned app only.'
  echo 'signed=false' >> "$GITHUB_OUTPUT"
  exit 0
fi
if [[ "$present" != 4 ]]; then
  echo '::error::Apple signing is partially configured. All four MOBILE_IOS_* secrets are required.'
  exit 1
fi
umask 077
python3 - <<'PY'
import base64, os
from pathlib import Path
for variable, filename in [("MOBILE_IOS_CERTIFICATE_P12_BASE64", "kharchiche-mobile.p12"),
                           ("MOBILE_IOS_PROVISION_PROFILE_BASE64", "kharchiche-mobile.mobileprovision")]:
    encoded = ''.join(os.environ[variable].split())
    Path(os.environ["RUNNER_TEMP"], filename).write_bytes(base64.b64decode(encoded, validate=True))
PY
security cms -D -i "$RUNNER_TEMP/kharchiche-mobile.mobileprovision" > "$RUNNER_TEMP/kharchiche-mobile-profile.plist"
uuid="$(python3 - <<'PY'
import datetime, os, plistlib, re
from pathlib import Path
p = plistlib.loads(Path(os.environ['RUNNER_TEMP'], 'kharchiche-mobile-profile.plist').read_bytes())
uuid = p['UUID']
if not re.fullmatch(r'[0-9A-Fa-f-]{36}', uuid): raise SystemExit('Invalid profile UUID')
if p['ExpirationDate'] <= datetime.datetime.now(datetime.timezone.utc).replace(tzinfo=None): raise SystemExit('Expired Apple profile')
identifier = p['Entitlements']['application-identifier'].split('.', 1)[1]
app_id = 'com.kharchiche.konsolidated'
matches = identifier == app_id or identifier == '*' or (identifier.endswith('.*') and app_id.startswith(identifier[:-1]))
if not matches:
    raise SystemExit('Provisioning profile does not match com.kharchiche.konsolidated')
print(uuid)
PY
)"
profile="$HOME/Library/MobileDevice/Provisioning Profiles/$uuid.mobileprovision"
profile_name="$(python3 - <<'PY'
import os, plistlib
from pathlib import Path
name = plistlib.loads(Path(os.environ['RUNNER_TEMP'], 'kharchiche-mobile-profile.plist').read_bytes())['Name']
if not name or '\n' in name or '\r' in name: raise SystemExit('Invalid provisioning profile name')
print(name)
PY
)"
mkdir -p "$(dirname "$profile")"
cp "$RUNNER_TEMP/kharchiche-mobile.mobileprovision" "$profile"
printf '%s' "$profile" > "$profile_record"
password="$(openssl rand -hex 24)"
echo "::add-mask::$password"
security create-keychain -p "$password" "$keychain"
security set-keychain-settings -lut 21600 "$keychain"
security unlock-keychain -p "$password" "$keychain"
security import "$RUNNER_TEMP/kharchiche-mobile.p12" -P "$MOBILE_IOS_CERTIFICATE_PASSWORD" -A -t cert -f pkcs12 -k "$keychain" >/dev/null
security list-keychains -d user -s "$keychain" "$HOME/Library/Keychains/login.keychain-db"
security set-key-partition-list -S apple-tool:,apple:,codesign: -s -k "$password" "$keychain" >/dev/null
security find-identity -v -p codesigning "$keychain" | grep -F -- "$MOBILE_IOS_SIGNING_IDENTITY" >/dev/null
echo "MOBILE_IOS_PROFILE_NAME=$profile_name" >> "$GITHUB_ENV"
echo 'MOBILE_IOS_SIGNED=true' >> "$GITHUB_ENV"
echo 'signed=true' >> "$GITHUB_OUTPUT"
