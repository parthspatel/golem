#!/bin/bash
# Copyright 2024-2025 Golem Cloud
#
# Script to generate Java bindings from WIT files using wit-bindgen
#
# Prerequisites:
#   - Rust toolchain installed
#   - wit-bindgen CLI installed from commit 86e8ae2b (last version with TeaVM-Java support)
#
# Install wit-bindgen with TeaVM-Java support:
#   cargo install --git https://github.com/bytecodealliance/wit-bindgen \
#       --rev 86e8ae2b8b97f11b73b273345b0e00340f017270 \
#       wit-bindgen-cli
#
# Usage:
#   ./scripts/generate-bindings.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
WIT_DIR="$PROJECT_DIR/wit"
OUTPUT_DIR="$PROJECT_DIR/src/main/java"

# Check for wit-bindgen
if ! command -v wit-bindgen &> /dev/null; then
    echo "Error: wit-bindgen not found. Install with:"
    echo "  cargo install --git https://github.com/bytecodealliance/wit-bindgen \\"
    echo "      --rev 86e8ae2b8b97f11b73b273345b0e00340f017270 \\"
    echo "      wit-bindgen-cli"
    exit 1
fi

# Check wit-bindgen version has teavm-java support
if ! wit-bindgen --help | grep -q "teavm-java"; then
    echo "Error: wit-bindgen does not have teavm-java support."
    echo "Install the correct version with:"
    echo "  cargo install --git https://github.com/bytecodealliance/wit-bindgen \\"
    echo "      --rev 86e8ae2b8b97f11b73b273345b0e00340f017270 \\"
    echo "      wit-bindgen-cli"
    exit 1
fi

echo "Generating Java bindings from WIT files..."

# Generate bindings for Golem host API (imports)
if [ -d "$WIT_DIR/deps/golem-1.x" ]; then
    echo "Generating golem:api bindings..."
    wit-bindgen guest teavm-java \
        "$WIT_DIR/deps/golem-1.x" \
        --out-dir "$OUTPUT_DIR/golem/api/bindings"
fi

# Generate bindings for WASI interfaces
if [ -d "$WIT_DIR/deps/io" ]; then
    echo "Generating wasi:io bindings..."
    wit-bindgen guest teavm-java \
        "$WIT_DIR/deps/io" \
        --out-dir "$OUTPUT_DIR/wasi/io"
fi

if [ -d "$WIT_DIR/deps/clocks" ]; then
    echo "Generating wasi:clocks bindings..."
    wit-bindgen guest teavm-java \
        "$WIT_DIR/deps/clocks" \
        --out-dir "$OUTPUT_DIR/wasi/clocks"
fi

echo "Binding generation complete!"
echo ""
echo "Generated files are in: $OUTPUT_DIR"
echo ""
echo "Note: You may need to manually adjust package names and imports"
echo "to match the cloud.golem.* package structure."
