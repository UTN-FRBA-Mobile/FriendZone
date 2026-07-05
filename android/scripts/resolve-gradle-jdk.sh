#!/usr/bin/env bash

resolve_gradle_jdk() {
  is_valid_jdk() {
    local home="$1"
    [[ -n "$home" && -x "$home/bin/java" && -x "$home/bin/jlink" ]]
  }

  is_unusable_jdk() {
    [[ "$1" == *".cursor/extensions"* || "$1" == *".vscode/extensions"* ]]
  }

  pick_jdk() {
    local candidate="$1"
    if is_valid_jdk "$candidate" && ! is_unusable_jdk "$candidate"; then
      printf '%s' "$candidate"
      return 0
    fi
    return 1
  }

  local resolved=""

  if resolved="$(pick_jdk "${JAVA_HOME:-}")"; then
    :
  elif [[ "$(uname -s)" == "Darwin" ]] && command -v /usr/libexec/java_home >/dev/null 2>&1; then
    local version
    for version in 21 17 22 11; do
      if resolved="$(pick_jdk "$(/usr/libexec/java_home -v "$version" 2>/dev/null || true)")"; then
        break
      fi
    done
  elif command -v java >/dev/null 2>&1; then
    local java_home_candidate
    java_home_candidate="$(java -XshowSettings:properties -version 2>&1 | awk -F'= ' '/java.home = / { print $2; exit }')"
    resolved="$(pick_jdk "$java_home_candidate" || true)"
  fi

  if [[ -z "$resolved" ]]; then
    echo "No suitable JDK found. Install Java 17+ and set JAVA_HOME, or ensure java/jlink are on PATH." >&2
    exit 1
  fi

  if [[ "${JAVA_HOME:-}" != "$resolved" ]]; then
    echo "Using JDK: $resolved" >&2
    GRADLE_JDK_CHANGED=1
  fi

  export JAVA_HOME="$resolved"
  export PATH="$JAVA_HOME/bin:$PATH"
}

run_gradlew() {
  resolve_gradle_jdk

  if [[ "${GRADLE_JDK_CHANGED:-0}" == "1" ]]; then
    ./gradlew --stop >/dev/null 2>&1 || true
  fi

  ./gradlew -Dorg.gradle.java.home="$JAVA_HOME" "$@"
}
