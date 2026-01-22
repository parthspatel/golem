# Golem JVM SDKs

JVM SDKs for building [Golem](https://golem.cloud) applications in Java, Scala, and Scala with ZIO.

## Modules

This repository contains three modules with a layered dependency structure:

```
golem-zio (Scala 3 + ZIO)
    ↓ depends on
golem-scala (Scala 3)
    ↓ depends on
golem-java (Java 17)
```

### golem-java

Pure Java SDK providing:
- Agent base classes and annotations (`@Agent`, `@Prompt`, `@Description`, `@Endpoint`)
- Type system (`Option`, `Result`, `Either`)
- Transaction API for durable operations
- Host API wrappers

### golem-scala

Scala SDK providing idiomatic Scala wrappers:
- Implicit conversions from Java types
- Pattern matching support for `Option`, `Result`, `Either`
- For-comprehension support
- Extension methods

### golem-zio

ZIO-native SDK providing:
- ZIO effects for all Golem operations
- ZLayer-based dependency injection
- Type-safe error handling
- ZIO Streams support
- JSON serialization via zio-json

## Installation

### sbt

```scala
// Java only
libraryDependencies += "cloud.golem" % "golem-java" % "0.0.0"

// Scala (includes Java)
libraryDependencies += "cloud.golem" %% "golem-scala" % "0.0.0"

// ZIO (includes Scala and Java)
libraryDependencies += "cloud.golem" %% "golem-zio" % "0.0.0"
```

### Maven

```xml
<!-- Java -->
<dependency>
    <groupId>cloud.golem</groupId>
    <artifactId>golem-java</artifactId>
    <version>0.0.0</version>
</dependency>

<!-- Scala -->
<dependency>
    <groupId>cloud.golem</groupId>
    <artifactId>golem-scala_3</artifactId>
    <version>0.0.0</version>
</dependency>

<!-- ZIO -->
<dependency>
    <groupId>cloud.golem</groupId>
    <artifactId>golem-zio_3</artifactId>
    <version>0.0.0</version>
</dependency>
```

## Quick Start

### Java

```java
import cloud.golem.java.agentic.*;
import cloud.golem.java.types.*;

@Agent
public class CounterAgent extends BaseAgent {
    private int count = 0;

    @Prompt("Increment the counter")
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
import cloud.golem.scala.agentic.*
import cloud.golem.scala.types.*
import cloud.golem.scala.types.given

@agent
class CounterAgent extends BaseAgent:
  private var count: Int = 0

  @prompt("Increment the counter")
  def increment(): Int =
    count += 1
    count

  def getCount: Int = count

// Use Scala-idiomatic APIs
val opt: Option[Int] = Option.some(42).toScala
val result = for
  a <- Option.some(1)
  b <- Option.some(2)
yield a + b
```

### Scala with ZIO

```scala
import cloud.golem.zio.*
import cloud.golem.zio.agentic.*
import cloud.golem.zio.host.*
import zio.*

@agent
class CounterAgent extends BaseAgent:
  private var count: Int = 0

  def increment: UIO[Int] = ZIO.succeed {
    count += 1
    count
  }

  def getCount: UIO[Int] = ZIO.succeed(count)

// Use ZIO effects
val program: ZIO[GolemHost, GolemError, Unit] =
  for
    promise <- GolemHost.createPromise
    _       <- GolemHost.awaitPromise(promise.id)
  yield ()
```

## Building

```bash
# Compile all modules
sbt compile

# Compile specific module
sbt golemJava/compile
sbt golemScala/compile
sbt golemZio/compile
```

## Testing

```bash
# Test all modules
sbt test

# Test specific module
sbt golemJava/test
sbt golemScala/test
sbt golemZio/test
```

## Documentation

For more information, visit [Golem Documentation](https://learn.golem.cloud).

## License

Apache-2.0
