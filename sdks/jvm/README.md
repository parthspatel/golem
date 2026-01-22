# Golem Java SDK

Java SDK for building [Golem](https://golem.cloud) WebAssembly components.

## Overview

This SDK enables Java developers to build durable, distributed applications that run on the Golem platform. Java code is compiled to WebAssembly using [TeaVM](https://teavm.org/), allowing it to run as a Golem component.

### Features

- **Host API Bindings** - Direct access to Golem runtime functions
- **Transaction API** - Infallible and fallible transactions with compensation
- **Promise API** - Async coordination between workers
- **Persistence Control** - Fine-grained control over durability

## Prerequisites

- Java 17+
- Maven 3.8+
- [wasm-tools](https://github.com/bytecodealliance/wasm-tools) (for componentization)
- [wit-bindgen](https://github.com/bytecodealliance/wit-bindgen) (commit 86e8ae2b for TeaVM-Java support)

### Installing wit-bindgen with TeaVM-Java Support

The TeaVM-Java generator was removed from mainline wit-bindgen. Install the last version that includes it:

```bash
cargo install --git https://github.com/bytecodealliance/wit-bindgen \
    --rev 86e8ae2b8b97f11b73b273345b0e00340f017270 \
    wit-bindgen-cli
```

## Project Structure

```
golem-java/
├── pom.xml                    # Maven build with TeaVM plugin
├── scripts/
│   └── generate-bindings.sh   # WIT binding generation script
├── src/main/java/cloud/golem/
│   ├── api/                   # Generated WIT bindings (after running script)
│   ├── host/                  # High-level host API wrappers
│   │   ├── GolemHost.java     # Main entry point for host functions
│   │   ├── Transaction.java   # Transaction API
│   │   ├── Promise.java       # Promise handling
│   │   └── ...
│   └── types/                 # Core types
│       ├── PromiseId.java
│       ├── WorkerId.java
│       ├── OplogIndex.java
│       └── ...
└── wit/                       # WIT interface definitions
```

## Quick Start

### 1. Generate WIT Bindings

First, ensure WIT files are synced (from repository root):
```bash
cargo make wit-sdks
```

Then generate Java bindings:
```bash
cd sdks/jvm/golem-java
./scripts/generate-bindings.sh
```

### 2. Write Your Component

```java
package com.example;

import cloud.golem.host.GolemHost;
import cloud.golem.host.Transaction;
import cloud.golem.types.PromiseId;

public class MyComponent {

    private int counter = 0;

    public int increment() {
        return Transaction.atomically(() -> {
            counter++;
            return counter;
        });
    }

    public int getCounter() {
        return counter;
    }

    // Async coordination example
    public void waitForSignal(PromiseId promiseId) {
        byte[] data = GolemHost.awaitPromise(promiseId);
        // Process the signal data
    }
}
```

### 3. Build WASM Component

```bash
# Compile to WASM
mvn clean compile

# Create component (requires wasm-tools)
mvn package -Pwasm
```

The output will be in `target/golem-java.wasm`.

### 4. Deploy to Golem

```bash
golem-cli component add --component-name my-component target/golem-java.wasm
golem-cli worker add --component-name my-component --worker-name worker-1
```

## API Reference

### GolemHost

Main entry point for Golem host functions:

```java
import cloud.golem.host.GolemHost;
import cloud.golem.types.*;

// Promises
PromiseId promiseId = GolemHost.createPromise();
byte[] result = GolemHost.awaitPromise(promiseId);
GolemHost.completePromise(promiseId, data);

// Oplog
OplogIndex index = GolemHost.getOplogIndex();
GolemHost.setOplogIndex(index);
GolemHost.oplogCommit();

// Persistence
PersistenceLevel level = GolemHost.getOplogPersistenceLevel();
GolemHost.setOplogPersistenceLevel(PersistenceLevel.PERSIST_IMMEDIATELY);

// Scoped persistence
try (var guard = GolemHost.usePersistenceLevel(PersistenceLevel.PERSIST_IMMEDIATELY)) {
    // Operations with immediate persistence
}

// Atomic operations
try (var guard = GolemHost.markBeginOperation()) {
    // Atomic operations
}
```

### Transaction API

For durable, recoverable operations:

```java
import cloud.golem.host.Transaction;

// Infallible transaction - will retry until success
String result = Transaction.infallible(tx -> {
    String a = tx.execute(
        () -> externalCall(),           // Operation
        r -> undoExternalCall(r)         // Compensation on retry
    );
    String b = tx.execute(
        () -> anotherCall(a),
        r -> undoAnotherCall(r)
    );
    return b;
});

// Fallible transaction - may abort with error
Transaction.Result<String, Error> result = Transaction.fallible(tx -> {
    try {
        String value = tx.execute(
            () -> riskyOperation(),
            r -> cleanup(r)
        );
        return Transaction.Result.ok(value);
    } catch (Exception e) {
        tx.abort(new Error(e.getMessage()));
        return null; // Never reached
    }
});

// Simple atomic block
Transaction.atomically(() -> {
    operation1();
    operation2();
});
```

## Building

### Compile Only
```bash
mvn compile
```

### Run Tests
```bash
mvn test
```

### Build WASM
```bash
mvn package -Pwasm
```

### Full Clean Build
```bash
mvn clean package -Pwasm
```

## Limitations

- **Experimental**: TeaVM WASM support is experimental
- **No Reflection**: Limited reflection support in TeaVM
- **Library Support**: Not all Java libraries work in WASM
- **WIT Bindings**: Requires manual regeneration when WIT interfaces change

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│  Your Java Code                                         │
│      ↓                                                  │
│  Golem Java SDK (cloud.golem.*)                        │
│      ↓                                                  │
│  WIT Bindings (generated by wit-bindgen)               │
│      ↓                                                  │
│  TeaVM Compiler                                         │
│      ↓                                                  │
│  Core WASM Module                                       │
│      ↓                                                  │
│  wasm-tools component new                              │
│      ↓                                                  │
│  WASM Component (runs in Golem)                        │
└─────────────────────────────────────────────────────────┘
```

## Related Projects

- [TeaVM](https://teavm.org/) - Java to WebAssembly compiler
- [golemcloud/teavm-wasi](https://github.com/golemcloud/teavm-wasi) - TeaVM fork with WASI support
- [wit-bindgen](https://github.com/bytecodealliance/wit-bindgen) - WIT binding generator

## License

Apache-2.0
