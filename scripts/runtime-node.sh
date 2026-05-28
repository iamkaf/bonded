#!/usr/bin/env bash
set -euo pipefail

if [ "$#" -lt 1 ] || [ "$#" -gt 3 ]; then
  echo "Usage: $0 <node> [timeout-seconds] [scenario]" >&2
  exit 1
fi

node="$1"
timeout_seconds="${2:-240}"
scenario="${3:-bonded/over-repair}"

cd "$(dirname "$0")/.."

version="${node%-*}"
loader="${node##*-}"
version_dir="$version"

if [ "$version" = "$node" ] || [ -z "$version" ] || [ -z "$loader" ]; then
  echo "Node must use <version>-<loader>, for example 26.1.2-fabric" >&2
  exit 1
fi

if [ ! -d "$version_dir" ] || [ ! -f "$version_dir/gradle.properties" ]; then
  echo "Version is not wired for Bonded runtime: $version" >&2
  exit 1
fi

enabled_loaders="$(
  sed -n 's/^project.enabled-loaders=//p' "$version_dir/gradle.properties" \
    | tr ',' '\n' \
    | sed 's/^[[:space:]]*//; s/[[:space:]]*$//'
)"

if ! printf '%s\n' "$enabled_loaders" | grep -qx "$loader"; then
  echo "Loader '$loader' is not enabled for Bonded version $version" >&2
  exit 1
fi

gradle="./gradlew"
gradle_task=":$loader:runClient"
scenario_file="test/scenarios/$scenario.json"

case "$loader" in
  fabric)
    instance_path="$loader/runs/client/teakit/instance.json"
    save_path="$loader/runs/client/saves/bonded_over_repair"
    ;;
  forge|neoforge)
    instance_path="$loader/run/teakit/instance.json"
    save_path="$loader/run/saves/bonded_over_repair"
    ;;
  *)
    echo "Unsupported loader: $loader" >&2
    exit 1
    ;;
esac

if [ ! -f "$version_dir/$scenario_file" ]; then
  echo "Scenario not found: $version_dir/$scenario_file" >&2
  exit 1
fi

log="/tmp/bonded-$node.runtime.run.log"
result="/tmp/bonded-$node.runtime.result.json"
health="/tmp/bonded-$node.runtime.health.json"

cd "$version_dir"
rm -f "$instance_path" "$log" "$result" "$health"
rm -rf "$save_path"

"$gradle" --max-workers=1 ":common:compileJava" ":$loader:compileJava" --console=plain

export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:-} -Dteakit.autoWorld=true -Dteakit.worldId=bonded_over_repair -Dteakit.worldName=Bonded_Over_Repair -Dteakit.repoRoot=$PWD -Dteakit.scenarioRoot=$PWD"

"$gradle" --max-workers=1 "$gradle_task" --console=plain \
  -Pbonded.withTeaKit=true \
  -Dteakit.autoWorld=true \
  -Dteakit.worldId="bonded_over_repair" \
  -Dteakit.worldName="Bonded Over Repair" \
  -Dteakit.repoRoot="$PWD" \
  -Dteakit.scenarioRoot="$PWD" \
  >"$log" 2>&1 &
gradle_pid=$!

port=""
token=""
base_url=""

cleanup() {
  set +e
  if [ -f "$instance_path" ]; then
    port="$(jq -r '.port' "$instance_path" 2>/dev/null || true)"
    token="$(jq -r '.token' "$instance_path" 2>/dev/null || true)"
    if [ -n "$port" ] && [ -n "$token" ] && [ "$port" != "null" ] && [ "$token" != "null" ]; then
      base_url="http://localhost:$port"
      curl -fsS -H "X-TeaKit-Token: $token" \
        -H 'Content-Type: application/json' \
        --data '{"delayMs":500}' \
        "$base_url/action/client/quit" >/dev/null 2>&1 || true
    fi
  fi
  if kill -0 "$gradle_pid" >/dev/null 2>&1; then
    wait "$gradle_pid" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

for _ in $(seq 1 "$timeout_seconds"); do
  if [ -f "$instance_path" ]; then
    port="$(jq -r '.port' "$instance_path" 2>/dev/null || true)"
    token="$(jq -r '.token' "$instance_path" 2>/dev/null || true)"
    base_url="http://localhost:$port"
    if [ -n "$port" ] && [ -n "$token" ] && [ "$port" != "null" ] && [ "$token" != "null" ] \
      && curl -fsS -H "X-TeaKit-Token: $token" "$base_url/health" >"$health" 2>/dev/null; then
      break
    fi
  fi
  if ! kill -0 "$gradle_pid" >/dev/null 2>&1; then
    wait "$gradle_pid"
    tail -n 160 "$log" >&2
    exit 1
  fi
  sleep 1
done

if [ ! -f "$health" ]; then
  tail -n 160 "$log" >&2
  exit 1
fi

world_ready=0
for _ in $(seq 1 "$timeout_seconds"); do
  if curl -fsS -H "X-TeaKit-Token: $token" "$base_url/health" >"$health" 2>/dev/null \
    && jq -e '.status.worldLoaded == true and .status.singleplayer == true and .status.playerCount > 0' "$health" >/dev/null 2>&1; then
    world_ready=1
    break
  fi
  if ! kill -0 "$gradle_pid" >/dev/null 2>&1; then
    wait "$gradle_pid"
    tail -n 160 "$log" >&2
    exit 1
  fi
  sleep 1
done

if [ "$world_ready" -ne 1 ]; then
  tail -n 160 "$log" >&2
  exit 1
fi

curl -fsS -H "X-TeaKit-Token: $token" \
  -H 'Content-Type: application/json' \
  --data '{}' \
  "$base_url/action/menu/close" >/dev/null || true

sleep 1

http_code="$(
  curl -sS -o "$result" -w '%{http_code}' \
    --max-time "$((timeout_seconds + 30))" \
    -H "X-TeaKit-Token: $token" \
    -H 'Content-Type: application/json' \
    --data-binary @"$scenario_file" \
    "$base_url/scenario/run"
)"
if [ "$http_code" != "200" ]; then
  cat "$result" >&2 || true
  tail -n 160 "$log" >&2
  exit 1
fi

expected_steps="$(jq '.steps | length' "$scenario_file")"
actual_steps="$(jq '.steps | length' "$result")"
if [ "$actual_steps" -lt "$expected_steps" ]; then
  cat "$result" >&2 || true
  exit 1
fi
jq -e '.name != null and (.error? | not)' "$result" >/dev/null

curl -fsS -H "X-TeaKit-Token: $token" \
  -H 'Content-Type: application/json' \
  --data '{"delayMs":500}' \
  "$base_url/action/client/quit" >/dev/null

wait "$gradle_pid"
grep -q 'Initializing TeaKit on' "$log"
echo "Bonded runtime OK: $node $scenario"
