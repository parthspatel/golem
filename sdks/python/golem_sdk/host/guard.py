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

"""Guards for temporarily changing runtime settings."""

from contextlib import contextmanager
from dataclasses import dataclass
from enum import Enum
from typing import Generator, Optional
from datetime import timedelta


class PersistenceLevel(Enum):
    """Persistence level for oplog entries."""

    PERSIST_NOTHING = "persist_nothing"
    PERSIST_REMOTE_SIDE_EFFECTS = "persist_remote_side_effects"
    SMART = "smart"


@dataclass
class RetryPolicy:
    """
    Configuration for retry behavior on failures.

    Attributes:
        max_attempts: Maximum number of retry attempts.
        min_delay: Minimum delay between retries.
        max_delay: Maximum delay between retries.
        multiplier: Multiplier for exponential backoff.
        max_jitter_factor: Optional jitter factor (0.0 to 1.0).
    """

    max_attempts: int = 3
    min_delay: timedelta = timedelta(seconds=1)
    max_delay: timedelta = timedelta(seconds=60)
    multiplier: float = 2.0
    max_jitter_factor: Optional[float] = None

    @classmethod
    def no_retry(cls) -> "RetryPolicy":
        """Create a policy that doesn't retry."""
        return cls(max_attempts=1)

    @classmethod
    def exponential_backoff(
        cls,
        max_attempts: int = 5,
        min_delay: timedelta = timedelta(seconds=1),
        max_delay: timedelta = timedelta(seconds=60),
        multiplier: float = 2.0,
    ) -> "RetryPolicy":
        """Create an exponential backoff retry policy."""
        return cls(
            max_attempts=max_attempts,
            min_delay=min_delay,
            max_delay=max_delay,
            multiplier=multiplier,
        )


# Global state for guards (in runtime, these would interface with the Golem host)
_current_persistence_level: PersistenceLevel = PersistenceLevel.SMART
_current_idempotence_mode: bool = True
_current_retry_policy: RetryPolicy = RetryPolicy()


@contextmanager
def use_persistence_level(
    level: PersistenceLevel,
) -> Generator[None, None, None]:
    """
    Temporarily set the oplog persistence level.

    The original level is restored when the context exits.

    Example:
        ```python
        with use_persistence_level(PersistenceLevel.PERSIST_NOTHING):
            # Operations here won't be persisted to the oplog
            local_computation()
        # Original persistence level is restored
        ```
    """
    global _current_persistence_level
    original = _current_persistence_level
    _current_persistence_level = level
    try:
        yield
    finally:
        _current_persistence_level = original


@contextmanager
def use_idempotence_mode(enabled: bool) -> Generator[None, None, None]:
    """
    Temporarily enable or disable idempotence mode.

    When idempotence mode is enabled, repeated calls to the same operation
    with the same idempotency key return the cached result.

    Example:
        ```python
        with use_idempotence_mode(False):
            # Operations here will always execute, even on replay
            result = non_idempotent_operation()
        ```
    """
    global _current_idempotence_mode
    original = _current_idempotence_mode
    _current_idempotence_mode = enabled
    try:
        yield
    finally:
        _current_idempotence_mode = original


@contextmanager
def use_retry_policy(policy: RetryPolicy) -> Generator[None, None, None]:
    """
    Temporarily set the retry policy.

    The original policy is restored when the context exits.

    Example:
        ```python
        aggressive_retry = RetryPolicy(
            max_attempts=10,
            min_delay=timedelta(milliseconds=100),
            max_delay=timedelta(seconds=5),
        )
        with use_retry_policy(aggressive_retry):
            result = flaky_external_call()
        ```
    """
    global _current_retry_policy
    original = _current_retry_policy
    _current_retry_policy = policy
    try:
        yield
    finally:
        _current_retry_policy = original


def get_persistence_level() -> PersistenceLevel:
    """Get the current persistence level."""
    return _current_persistence_level


def get_idempotence_mode() -> bool:
    """Get the current idempotence mode."""
    return _current_idempotence_mode


def get_retry_policy() -> RetryPolicy:
    """Get the current retry policy."""
    return _current_retry_policy
