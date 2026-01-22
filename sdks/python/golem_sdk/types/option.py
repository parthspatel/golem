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

"""Option type for representing optional values."""

from typing import Callable, Generic, Optional, TypeVar, Union, overload

T = TypeVar("T")
U = TypeVar("U")


class Option(Generic[T]):
    """
    Represents an optional value that may or may not be present.

    This is similar to Rust's Option type or Haskell's Maybe type.

    Example:
        ```python
        value: Option[int] = Option.some(42)
        empty: Option[int] = Option.none()

        # Check if value is present
        if value.is_some():
            print(value.unwrap())  # 42

        # Map over the value
        doubled = value.map(lambda x: x * 2)  # Option.some(84)

        # Get with default
        result = empty.unwrap_or(0)  # 0
        ```
    """

    __slots__ = ("_value", "_is_some")

    def __init__(self, value: Optional[T], is_some: bool) -> None:
        self._value = value
        self._is_some = is_some

    @classmethod
    def some(cls, value: T) -> "Option[T]":
        """Create an Option containing a value."""
        return cls(value, True)

    @classmethod
    def none(cls) -> "Option[T]":
        """Create an empty Option."""
        return cls(None, False)

    @classmethod
    def from_optional(cls, value: Optional[T]) -> "Option[T]":
        """Create an Option from a Python Optional."""
        if value is None:
            return cls.none()
        return cls.some(value)

    def is_some(self) -> bool:
        """Check if the Option contains a value."""
        return self._is_some

    def is_none(self) -> bool:
        """Check if the Option is empty."""
        return not self._is_some

    def unwrap(self) -> T:
        """
        Get the contained value.

        Raises:
            ValueError: If the Option is empty.
        """
        if not self._is_some:
            raise ValueError("Called unwrap on a None Option")
        return self._value  # type: ignore

    def unwrap_or(self, default: T) -> T:
        """Get the contained value or a default."""
        if self._is_some:
            return self._value  # type: ignore
        return default

    def unwrap_or_else(self, f: Callable[[], T]) -> T:
        """Get the contained value or compute a default."""
        if self._is_some:
            return self._value  # type: ignore
        return f()

    def map(self, f: Callable[[T], U]) -> "Option[U]":
        """Transform the contained value if present."""
        if self._is_some:
            return Option.some(f(self._value))  # type: ignore
        return Option.none()

    def flat_map(self, f: Callable[[T], "Option[U]"]) -> "Option[U]":
        """Transform the contained value with a function that returns an Option."""
        if self._is_some:
            return f(self._value)  # type: ignore
        return Option.none()

    def filter(self, predicate: Callable[[T], bool]) -> "Option[T]":
        """Keep the value only if it satisfies the predicate."""
        if self._is_some and predicate(self._value):  # type: ignore
            return self
        return Option.none()

    def to_optional(self) -> Optional[T]:
        """Convert to a Python Optional."""
        if self._is_some:
            return self._value
        return None

    def __eq__(self, other: object) -> bool:
        if not isinstance(other, Option):
            return NotImplemented
        if self._is_some != other._is_some:
            return False
        if not self._is_some:
            return True
        return self._value == other._value

    def __repr__(self) -> str:
        if self._is_some:
            return f"Option.some({self._value!r})"
        return "Option.none()"

    def __hash__(self) -> int:
        if self._is_some:
            return hash(("some", self._value))
        return hash(("none",))


# Convenience functions
def some(value: T) -> Option[T]:
    """Create an Option containing a value."""
    return Option.some(value)


def none() -> Option[T]:
    """Create an empty Option."""
    return Option.none()
