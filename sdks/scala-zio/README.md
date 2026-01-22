# golem-zio

Scala SDK with ZIO integration for building [Golem](https://golem.cloud) applications.

## Overview

The `golem-zio` package provides a ZIO-native SDK for building durable, distributed applications on the Golem platform. It includes:

- **ZIO effects** for all Golem operations
- **Agent base traits** with ZIO integration
- **Type-safe error handling** using ZIO's error channel
- **ZLayer-based dependency injection**
- **Stream support** for processing data
- **JSON serialization** via zio-json

## Installation

### sbt

```scala
libraryDependencies += "cloud.golem" %% "golem-zio" % "0.0.0"
```

### Mill

```scala
ivy"cloud.golem::golem-zio:0.0.0"
```

## Quick Start

```scala
import cloud.golem.zio._
import cloud.golem.zio.agentic._
import zio._

@agent
class CounterAgent extends BaseAgent:
  private var count: Int = 0

  def increment: ZIO[Any, Nothing, Int] =
    ZIO.succeed {
      count += 1
      count
    }

  def getCount: UIO[Int] = ZIO.succeed(count)
```

## Features

### ZIO Effects

All Golem operations return ZIO effects:

```scala
import cloud.golem.zio.host._
import zio._

// Create a promise
val program: ZIO[GolemHost, GolemError, PromiseId] =
  for {
    promise <- GolemHost.createPromise
    _       <- GolemHost.awaitPromise(promise.id)
  } yield promise.id
```

### Transaction API

Use ZIO-native transactions:

```scala
import cloud.golem.zio.host._

val atomicOperation: ZIO[GolemHost, GolemError, Unit] =
  Transaction.atomically {
    for {
      _ <- externalCall1
      _ <- externalCall2
    } yield ()
  }
```

### Type-Safe Errors

Use the error channel for type-safe error handling:

```scala
import cloud.golem.zio.types._

val result: ZIO[Any, GolemError, Int] =
  ZIO.fromEither(Result.ok(42).toEither)

val handled: ZIO[Any, Nothing, Int] =
  result.catchAll(error => ZIO.succeed(0))
```

### ZLayer Integration

Compose dependencies using ZLayer:

```scala
import cloud.golem.zio.host._
import zio._

val program: ZIO[GolemHost, GolemError, Unit] = ???

val runnable = program.provide(GolemHost.live)
```

### Option/Result Types

ZIO-friendly Option and Result types:

```scala
import cloud.golem.zio.types._

val opt: GolemOption[Int] = GolemOption.some(42)
val result: GolemResult[Int, String] = GolemResult.ok(42)

// Convert to ZIO
val zioOpt: IO[NoSuchElementException, Int] = opt.toZIO
val zioResult: IO[String, Int] = result.toZIO
```

### Streaming Support

Process data with ZIO Streams:

```scala
import zio.stream._

val stream: ZStream[GolemHost, GolemError, Byte] = ???
val processed = stream.mapZIO(process)
```

## Configuration

### Retry Policy

Configure retry behavior:

```scala
import cloud.golem.zio.host._
import zio._

val customRetry = RetryPolicy(
  maxAttempts = 5,
  minDelay = 1.second,
  maxDelay = 30.seconds,
  multiplier = 2.0
)

val withRetry = GolemHost.useRetryPolicy(customRetry) {
  flakyOperation
}
```

### Persistence Level

Control oplog persistence:

```scala
import cloud.golem.zio.host._

val withPersistence = GolemHost.usePersistenceLevel(PersistenceLevel.Smart) {
  operations
}
```

## Building

```bash
sbt compile
```

## Testing

```bash
sbt test
```

## Documentation

For more information, visit [Golem Documentation](https://learn.golem.cloud).

## License

Apache-2.0
