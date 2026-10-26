#!/bin/sh
set -eu
cd "$(git rev-parse --show-toplevel)"
command -v python3 >/dev/null || { echo 'Python 3 is required.' >&2; exit 1; }
local_scanner=$(git rev-parse --git-path tools/gitleaks)
if [ ! -x "$local_scanner" ] && ! command -v gitleaks >/dev/null; then
    echo 'Install Gitleaks 8.30.1 or later first (brew install gitleaks).' >&2
    exit 1
fi
chmod +x .githooks/pre-commit .githooks/pre-push
git config --local core.hooksPath .githooks
echo 'Commit and push secret checks enabled for this repository.'
