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

"""Either type for representing one of two values."""

from typing import Callable, Generic, TypeVar, Union

L = TypeVar("L")
R = TypeVar("R")
A = TypeVar("A")
B = TypeVar("B")


class Either(Generic[L, R]):
    """
    Represents a value that can be one of two types: Left or Right.

    This is similar to Haskell's Either type or Scala's Either type.
    By convention, Left is used for "failure" or "alternative" cases,
    while Right is the "success" or "primary" case.

    Example:
        ```python
        left_value: Either[str, int] = Either.left("error")
        right_value: Either[str, int] = Either.right(42)

        # Check which side
        if right_value.is_right():
            print(right_value.unwrap_right())  # 42

        # Map over the right value
        doubled = right_value.map(lambda x: x * 2)  # Either.right(84)

        # Handle both cases
        result = right_value.fold(
            left=lambda s: f"Error: {s}",
            right=lambda n: f"Got: {n}",
        )
        ```
    """

    __slots__ = ("_value", "_is_right")

    def __init__(self, value: Union[L, R], is_right: bool) -> None:
        self._value = value
        self._is_right = is_right

    @classmethod
    def left(cls, value: L) -> "Either[L, R]":
        """Create a Left value."""
        return cls(value, False)

    @classmethod
    def right(cls, value: R) -> "Either[L, R]":
        """Create a Right value."""
        return cls(value, True)

    def is_left(self) -> bool:
        """Check if this is a Left value."""
        return not self._is_right

    def is_right(self) -> bool:
        """Check if this is a Right value."""
        return self._is_right

    def unwrap_left(self) -> L:
        """
        Get the Left value.

        Raises:
            ValueError: If this is a Right value.
        """
        if self._is_right:
            raise ValueError(f"Called unwrap_left on a Right: {self._value}")
        return self._value  # type: ignore

    def unwrap_right(self) -> R:
        """
        Get the Right value.

        Raises:
            ValueError: If this is a Left value.
        """
        if not self._is_right:
            raise ValueError(f"Called unwrap_right on a Left: {self._value}")
        return self._value  # type: ignore

    def map(self, f: Callable[[R], A]) -> "Either[L, A]":
        """Transform the Right value if present."""
        if self._is_right:
            return Either.right(f(self._value))  # type: ignore
        return Either.left(self._value)  # type: ignore

    def map_left(self, f: Callable[[L], A]) -> "Either[A, R]":
        """Transform the Left value if present."""
        if not self._is_right:
            return Either.left(f(self._value))  # type: ignore
        return Either.right(self._value)  # type: ignore

    def flat_map(self, f: Callable[[R], "Either[L, A]"]) -> "Either[L, A]":
        """Transform the Right value with a function that returns an Either."""
        if self._is_right:
            return f(self._value)  # type: ignore
        return Either.left(self._value)  # type: ignore

    def fold(
        self,
        left: Callable[[L], A],
        right: Callable[[R], A],
    ) -> A:
        """Handle both cases and produce a single result."""
        if self._is_right:
            return right(self._value)  # type: ignore
        return left(self._value)  # type: ignore

    def swap(self) -> "Either[R, L]":
        """Swap Left and Right."""
        if self._is_right:
            return Either.left(self._value)  # type: ignore
        return Either.right(self._value)  # type: ignore

    def __eq__(self, other: object) -> bool:
        if not isinstance(other, Either):
            return NotImplemented
        if self._is_right != other._is_right:
            return False
        return self._value == other._value

    def __repr__(self) -> str:
        if self._is_right:
            return f"Either.right({self._value!r})"
        return f"Either.left({self._value!r})"

    def __hash__(self) -> int:
        if self._is_right:
            return hash(("right", self._value))
        return hash(("left", self._value))


# Convenience functions
def left(value: L) -> Either[L, R]:
    """Create a Left value."""
    return Either.left(value)


def right(value: R) -> Either[L, R]:
    """Create a Right value."""
    return Either.right(value)
