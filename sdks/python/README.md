# golem-sdk

Python SDK for building [Golem](https://golem.cloud) applications.

## Overview

The `golem-sdk` package provides Python utilities for building durable, distributed applications on the Golem platform. It includes:

- **Agent base classes** for building Golem agents
- **Type mappings** for Golem's type system
- **Transaction API** for durable, atomic operations
- **Host API wrappers** for interacting with Golem's runtime

## Installation

```bash
pip install golem-sdk
```

## Quick Start

```python
from golem_sdk import BaseAgent, agent, prompt, description
from golem_sdk.types import DataValue, Result

@agent
class MyAgent(BaseAgent):
    """A simple Golem agent."""

    def __init__(self, name: str):
        super().__init__()
        self.name = name

    @prompt("Greet the user")
    @description("Returns a greeting message")
    def greet(self) -> str:
        return f"Hello from {self.name}!"
```

## Features

### Agent Development

Create agents using the `@agent` decorator:

```python
from golem_sdk import BaseAgent, agent

@agent
class CounterAgent(BaseAgent):
    def __init__(self):
        super().__init__()
        self.count = 0

    def increment(self) -> int:
        self.count += 1
        return self.count

    def get_count(self) -> int:
        return self.count
```

### Transaction API

Use the transaction API for durable operations:

```python
from golem_sdk.host import transaction, atomically

# Using context manager
with transaction():
    # These operations are atomic
    result1 = external_call_1()
    result2 = external_call_2()

# Using decorator
@atomically
def atomic_operation():
    # All operations in this function are atomic
    pass
```

### Type System

The SDK provides Python mappings for Golem's type system:

```python
from golem_sdk.types import (
    DataValue,
    Result,
    Option,
    Either,
    Record,
    Variant,
)

# Option type
value: Option[str] = Option.some("hello")
empty: Option[str] = Option.none()

# Result type
success: Result[int, str] = Result.ok(42)
failure: Result[int, str] = Result.err("error")
```

## Documentation

For more information, visit [Golem Documentation](https://learn.golem.cloud).

## License

Apache-2.0
