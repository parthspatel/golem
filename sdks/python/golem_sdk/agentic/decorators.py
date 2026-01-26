# Copyright 2024-2025 Golem Cloud
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""Decorators for building Golem agents."""

from functools import wraps
from typing import (
    Any,
    Callable,
    Dict,
    List,
    Optional,
    Type,
    TypeVar,
    Union,
    overload,
)
from dataclasses import dataclass, field

T = TypeVar("T")
F = TypeVar("F", bound=Callable[..., Any])


# Registry for agent types
_agent_registry: Dict[str, Type[Any]] = {}


@dataclass
class AgentMetadata:
    """Metadata for an agent class."""

    name: str
    description: Optional[str] = None
    methods: Dict[str, "MethodMetadata"] = field(default_factory=dict)


@dataclass
class MethodMetadata:
    """Metadata for an agent method."""

    name: str
    description: Optional[str] = None
    prompt: Optional[str] = None
    is_endpoint: bool = False
    http_method: Optional[str] = None
    http_path: Optional[str] = None


def get_agent_registry() -> Dict[str, Type[Any]]:
    """Get the global agent registry."""
    return _agent_registry


@overload
def agent(cls: Type[T]) -> Type[T]: ...


@overload
def agent(
    *,
    name: Optional[str] = None,
    description: Optional[str] = None,
) -> Callable[[Type[T]], Type[T]]: ...


def agent(
    cls: Optional[Type[T]] = None,
    *,
    name: Optional[str] = None,
    description: Optional[str] = None,
) -> Union[Type[T], Callable[[Type[T]], Type[T]]]:
    """
    Decorator to mark a class as a Golem agent.

    Can be used with or without arguments:

        @agent
        class MyAgent(BaseAgent):
            pass

        @agent(name="custom-name", description="My agent")
        class MyAgent(BaseAgent):
            pass

    Args:
        cls: The class to decorate (when used without parentheses).
        name: Optional custom name for the agent.
        description: Optional description of the agent.

    Returns:
        The decorated class.
    """

    def decorator(cls: Type[T]) -> Type[T]:
        agent_name = name or cls.__name__

        # Create metadata
        metadata = AgentMetadata(
            name=agent_name,
            description=description or cls.__doc__,
        )

        # Collect method metadata
        for attr_name in dir(cls):
            if attr_name.startswith("_"):
                continue
            attr = getattr(cls, attr_name)
            if callable(attr) and hasattr(attr, "_golem_metadata"):
                metadata.methods[attr_name] = attr._golem_metadata

        # Store metadata on the class
        cls._golem_agent_metadata = metadata  # type: ignore

        # Register the agent
        _agent_registry[agent_name] = cls

        return cls

    if cls is not None:
        # Called without parentheses: @agent
        return decorator(cls)
    else:
        # Called with parentheses: @agent(...)
        return decorator


def prompt(prompt_text: str) -> Callable[[F], F]:
    """
    Decorator to add a prompt to an agent method.

    The prompt is used by AI systems to understand how to use the method.

    Args:
        prompt_text: The prompt text describing the method's purpose.

    Returns:
        The decorated function.

    Example:
        @prompt("Calculate the sum of two numbers")
        def add(self, a: int, b: int) -> int:
            return a + b
    """

    def decorator(func: F) -> F:
        if not hasattr(func, "_golem_metadata"):
            func._golem_metadata = MethodMetadata(name=func.__name__)  # type: ignore
        func._golem_metadata.prompt = prompt_text  # type: ignore
        return func

    return decorator


def description(desc: str) -> Callable[[F], F]:
    """
    Decorator to add a description to an agent method.

    The description provides documentation for the method.

    Args:
        desc: The description text.

    Returns:
        The decorated function.

    Example:
        @description("Returns the current count value")
        def get_count(self) -> int:
            return self.count
    """

    def decorator(func: F) -> F:
        if not hasattr(func, "_golem_metadata"):
            func._golem_metadata = MethodMetadata(name=func.__name__)  # type: ignore
        func._golem_metadata.description = desc  # type: ignore
        return func

    return decorator


def endpoint(
    *,
    method: str = "GET",
    path: Optional[str] = None,
) -> Callable[[F], F]:
    """
    Decorator to expose an agent method as an HTTP endpoint.

    Args:
        method: The HTTP method (GET, POST, PUT, DELETE, etc.).
        path: The URL path for the endpoint. Defaults to the method name.

    Returns:
        The decorated function.

    Example:
        @endpoint(method="POST", path="/items")
        def create_item(self, name: str) -> Item:
            return Item(name=name)
    """

    def decorator(func: F) -> F:
        if not hasattr(func, "_golem_metadata"):
            func._golem_metadata = MethodMetadata(name=func.__name__)  # type: ignore
        func._golem_metadata.is_endpoint = True  # type: ignore
        func._golem_metadata.http_method = method  # type: ignore
        func._golem_metadata.http_path = path or f"/{func.__name__}"  # type: ignore
        return func

    return decorator
