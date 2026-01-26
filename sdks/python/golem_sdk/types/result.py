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

"""Result type for representing success or failure."""

from typing import Callable, Generic, TypeVar, Union

T = TypeVar("T")
E = TypeVar("E")
U = TypeVar("U")
F = TypeVar("F")


class Result(Generic[T, E]):
    """
    Represents either a success value (Ok) or an error value (Err).

    This is similar to Rust's Result type.

    Example:
        ```python
        success: Result[int, str] = Result.ok(42)
        failure: Result[int, str] = Result.err("Something went wrong")

        # Check result
        if success.is_ok():
            print(success.unwrap())  # 42

        # Map over success value
        doubled = success.map(lambda x: x * 2)  # Result.ok(84)

        # Handle both cases
        result = success.match(
            ok=lambda x: f"Got {x}",
            err=lambda e: f"Error: {e}",
        )
        ```
    """

    __slots__ = ("_value", "_is_ok")

    def __init__(self, value: Union[T, E], is_ok: bool) -> None:
        self._value = value
        self._is_ok = is_ok

    @classmethod
    def ok(cls, value: T) -> "Result[T, E]":
        """Create a successful Result."""
        return cls(value, True)

    @classmethod
    def err(cls, error: E) -> "Result[T, E]":
        """Create a failed Result."""
        return cls(error, False)

    def is_ok(self) -> bool:
        """Check if the Result is successful."""
        return self._is_ok

    def is_err(self) -> bool:
        """Check if the Result is an error."""
        return not self._is_ok

    def unwrap(self) -> T:
        """
        Get the success value.

        Raises:
            ValueError: If the Result is an error.
        """
        if not self._is_ok:
            raise ValueError(f"Called unwrap on an Err Result: {self._value}")
        return self._value  # type: ignore

    def unwrap_err(self) -> E:
        """
        Get the error value.

        Raises:
            ValueError: If the Result is successful.
        """
        if self._is_ok:
            raise ValueError(f"Called unwrap_err on an Ok Result: {self._value}")
        return self._value  # type: ignore

    def unwrap_or(self, default: T) -> T:
        """Get the success value or a default."""
        if self._is_ok:
            return self._value  # type: ignore
        return default

    def unwrap_or_else(self, f: Callable[[E], T]) -> T:
        """Get the success value or compute a default from the error."""
        if self._is_ok:
            return self._value  # type: ignore
        return f(self._value)  # type: ignore

    def map(self, f: Callable[[T], U]) -> "Result[U, E]":
        """Transform the success value if present."""
        if self._is_ok:
            return Result.ok(f(self._value))  # type: ignore
        return Result.err(self._value)  # type: ignore

    def map_err(self, f: Callable[[E], F]) -> "Result[T, F]":
        """Transform the error value if present."""
        if self._is_ok:
            return Result.ok(self._value)  # type: ignore
        return Result.err(f(self._value))  # type: ignore

    def flat_map(self, f: Callable[[T], "Result[U, E]"]) -> "Result[U, E]":
        """Transform the success value with a function that returns a Result."""
        if self._is_ok:
            return f(self._value)  # type: ignore
        return Result.err(self._value)  # type: ignore

    def match(
        self,
        ok: Callable[[T], U],
        err: Callable[[E], U],
    ) -> U:
        """Pattern match on the Result, handling both cases."""
        if self._is_ok:
            return ok(self._value)  # type: ignore
        return err(self._value)  # type: ignore

    def __eq__(self, other: object) -> bool:
        if not isinstance(other, Result):
            return NotImplemented
        if self._is_ok != other._is_ok:
            return False
        return self._value == other._value

    def __repr__(self) -> str:
        if self._is_ok:
            return f"Result.ok({self._value!r})"
        return f"Result.err({self._value!r})"

    def __hash__(self) -> int:
        if self._is_ok:
            return hash(("ok", self._value))
        return hash(("err", self._value))


# Convenience functions
def ok(value: T) -> Result[T, E]:
    """Create a successful Result."""
    return Result.ok(value)


def err(error: E) -> Result[T, E]:
    """Create a failed Result."""
    return Result.err(error)
