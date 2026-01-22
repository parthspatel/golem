# golem-java-sdk

Java and Scala SDK for building [Golem](https://golem.cloud) applications.

## Overview

The `golem-sdk` package provides utilities for building durable, distributed applications on the Golem platform using Java or Scala. It includes:

- **Agent base classes** for building Golem agents
- **Type mappings** for Golem's type system
- **Transaction API** for durable, atomic operations
- **Host API wrappers** for interacting with Golem's runtime

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("cloud.golem:golem-sdk:0.0.0")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'cloud.golem:golem-sdk:0.0.0'
}
```

### Maven

```xml
<dependency>
    <groupId>cloud.golem</groupId>
    <artifactId>golem-sdk</artifactId>
    <version>0.0.0</version>
</dependency>
```

## Quick Start

### Java

```java
import cloud.golem.sdk.agentic.BaseAgent;
import cloud.golem.sdk.agentic.Agent;

@Agent
public class CounterAgent extends BaseAgent {
    private int count = 0;

    public int increment() {
        count++;
        return count;
    }

    public int getCount() {
        return count;
    }
}
```

### Scala

```scala
import cloud.golem.sdk.agentic.{BaseAgent, Agent}

@Agent
class CounterAgent extends BaseAgent {
  private var count: Int = 0

  def increment(): Int = {
    count += 1
    count
  }

  def getCount: Int = count
}
```

## Features

### Agent Development

Create agents using the `@Agent` annotation:

```java
@Agent(name = "my-counter")
public class MyCounter extends BaseAgent {
    // Agent implementation
}
```

### Transaction API

Use the transaction API for durable operations:

```java
import cloud.golem.sdk.host.Transaction;

Transaction.atomically(() -> {
    // These operations are atomic
    externalCall1();
    externalCall2();
});
```

### Type System

The SDK provides Java/Scala mappings for Golem's type system:

```java
import cloud.golem.sdk.types.*;

// Option type
Option<String> value = Option.some("hello");
Option<String> empty = Option.none();

// Result type
Result<Integer, String> success = Result.ok(42);
Result<Integer, String> failure = Result.err("error");
```

## Scala-Specific Features

The SDK includes Scala-idiomatic APIs:

```scala
import cloud.golem.sdk.types._

// Pattern matching support
val result: Result[Int, String] = Result.ok(42)
result match {
  case Result.Ok(value) => println(s"Got $value")
  case Result.Err(error) => println(s"Error: $error")
}

// For-comprehension support
for {
  a <- Option.some(1)
  b <- Option.some(2)
} yield a + b
```

## Building

```bash
./gradlew build
```

## Testing

```bash
./gradlew test
```

## Documentation

For more information, visit [Golem Documentation](https://learn.golem.cloud).

## License

Apache-2.0
