#!/usr/bin/env bash
# OMS-POC JMeter benchmark runner
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PLAN="$ROOT/benchmark/jmeter/oms-place-order-benchmark.jmx"
RESULTS="$ROOT/benchmark/jmeter/results"
mkdir -p "$RESULTS"

PRODUCT_ID="${1:-}"
MODE="${2:-both}"
if [[ -z "$PRODUCT_ID" ]]; then
  echo "Usage: ./benchmark/run-jmeter.sh <product-uuid> [saga|camel|both]"
  exit 1
fi

TS="$(date +%Y%m%d_%H%M%S)"
jmeter -n -t "$PLAN" \
  -l "$RESULTS/${MODE}-${TS}.jtl" \
  -e -o "$RESULTS/report-${MODE}-${TS}" \
  -JPRODUCT_ID="$PRODUCT_ID" \
  -JBENCH_MODE="$MODE" \
  -JIDENTITY_HOST=127.0.0.1 -JIDENTITY_PORT=8086 \
  -JINTEGRATION_HOST=127.0.0.1 -JINTEGRATION_PORT=8085 \
  -JTHREADS=10 -JRAMP_UP=5 -JLOOPS=20

echo "Report: $RESULTS/report-${MODE}-${TS}/index.html"
