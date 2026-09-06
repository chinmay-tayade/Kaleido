#!/usr/bin/env bash
# Regenerate the static web bundle that Vercel serves (web/).
# Run from the repo root:  ./scripts/build-web.sh
set -euo pipefail

cd "$(dirname "$0")/.."

echo "▶ Building Kotlin/Wasm production bundle…"
./gradlew :composeApp:wasmJsBrowserDistribution

echo "▶ Refreshing web/…"
rm -rf web
mkdir -p web
cp -R composeApp/build/dist/wasmJs/productionExecutable/. web/

echo "✓ web/ updated ($(du -sh web | cut -f1)). Commit and push to redeploy on Vercel."
