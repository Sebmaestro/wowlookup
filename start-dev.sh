#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

(cd "$root_dir" && ./mvnw spring-boot:run) &
(cd "$root_dir/frontend" && npm run dev) &

wait