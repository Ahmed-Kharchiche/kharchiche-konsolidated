#!/usr/bin/env bash
# CI-only: callers download the verified artifacts of successful jobs in this run.
set -euo pipefail
[[ "${GITHUB_ACTIONS:-}" == true && "${GITHUB_REF:-}" == refs/heads/main ]] || {
  echo 'Mobile publication is restricted to GitHub Actions on main.' >&2; exit 1;
}
export GIT_LFS_SKIP_SMUDGE=1
git lfs install --local
git config user.name 'github-actions[bot]'
git config user.email '41898282+github-actions[bot]@users.noreply.github.com'
paths=()
if [[ "${PUBLISH_ANDROID:-false}" == true ]]; then paths+=(dist/android/Kharchiche-Konsolidated-1.0.0.apk); fi
if [[ "${PUBLISH_IOS:-false}" == true ]]; then paths+=(dist/ios/Kharchiche-Konsolidated-1.0.0.ipa); fi
[[ ${#paths[@]} -gt 0 ]] || { echo 'No verified mobile package to publish.'; exit 0; }
git add -f -- "${paths[@]}"
if git diff --cached --quiet; then echo 'No mobile distribution changes.'; exit 0; fi
git commit -m 'chore(dist): update verified mobile distributions [skip ci]'
# Never force-push. Preserve unrelated commits that arrive while publishing.
for attempt in 1 2 3; do
  if git push origin HEAD:main; then exit 0; fi
  if [[ "$attempt" == 3 ]]; then break; fi
  git fetch origin main
  git -c core.editor=true rebase origin/main || {
    git rebase --abort
    echo 'Publication conflict: existing remote distributions were preserved.' >&2
    exit 1
  }
done
echo 'Publication failed; existing remote distributions were preserved.' >&2
exit 1
