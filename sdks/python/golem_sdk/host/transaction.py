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

"""Transaction API for durable, atomic operations."""

from contextlib import contextmanager
from functools import wraps
from typing import Any, Callable, Generator, List, Optional, TypeVar

T = TypeVar("T")
F = TypeVar("F", bound=Callable[..., Any])


class Transaction:
    """
    A transaction context for grouping operations atomically.

    Transactions ensure that a group of operations either all succeed
    or all fail together. In case of failure during replay, the entire
    transaction is re-executed.

    Example:
        ```python
        tx = Transaction()
        tx.begin()
        try:
            result1 = external_call_1()
            result2 = external_call_2()
            tx.commit()
        except Exception:
            tx.rollback()
            raise
        ```
    """

    def __init__(self) -> None:
        self._operations: List[Callable[[], None]] = []
        self._compensations: List[Callable[[], None]] = []
        self._begin_idx: Optional[int] = None

    def begin(self) -> None:
        """Begin a new transaction."""
        # In the actual runtime, this would call mark_begin_operation()
        self._begin_idx = 0
        self._operations = []
        self._compensations = []

    def commit(self) -> None:
        """Commit the transaction."""
        # In the actual runtime, this would call mark_end_operation()
        self._begin_idx = None

    def rollback(self) -> None:
        """Rollback the transaction by executing compensating actions."""
        for compensation in reversed(self._compensations):
            try:
                compensation()
            except Exception:
                # Log but continue with other compensations
                pass
        self._operations = []
        self._compensations = []
        self._begin_idx = None

    def add_compensation(self, compensation: Callable[[], None]) -> None:
        """Add a compensation action to be executed on rollback."""
        self._compensations.append(compensation)


@contextmanager
def transaction() -> Generator[Transaction, None, None]:
    """
    Context manager for atomic transactions.

    All operations within the context are treated as a single atomic unit.
    In case of failure during replay, the entire block is re-executed.

    Example:
        ```python
        with transaction() as tx:
            result1 = external_call_1()
            result2 = external_call_2()
        ```
    """
    tx = Transaction()
    tx.begin()
    try:
        yield tx
        tx.commit()
    except Exception:
        tx.rollback()
        raise


def atomically(func: F) -> F:
    """
    Decorator to make a function execute as an atomic transaction.

    The entire function body is treated as a single atomic unit.
    In case of failure during replay, the entire function is re-executed.

    Example:
        ```python
        @atomically
        def transfer(from_account: str, to_account: str, amount: int) -> None:
            withdraw(from_account, amount)
            deposit(to_account, amount)
        ```
    """

    @wraps(func)
    def wrapper(*args: Any, **kwargs: Any) -> Any:
        with transaction():
            return func(*args, **kwargs)

    return wrapper  # type: ignore


@contextmanager
def fallible_transaction() -> Generator[Transaction, None, None]:
    """
    Context manager for transactions that may fail without re-execution.

    Unlike `transaction()`, operations in a fallible transaction are not
    automatically retried on failure. Use this when you want to handle
    failures explicitly.

    Example:
        ```python
        with fallible_transaction() as tx:
            try:
                result = risky_operation()
            except Exception as e:
                tx.add_compensation(lambda: cleanup())
                raise
        ```
    """
    tx = Transaction()
    tx.begin()
    try:
        yield tx
        tx.commit()
    except Exception:
        tx.rollback()
        raise
