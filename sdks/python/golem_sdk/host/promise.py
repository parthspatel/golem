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

"""Promise API for async coordination."""

from dataclasses import dataclass
from typing import Optional
import asyncio

from golem_sdk.agentic.agent_id import AgentId


@dataclass(frozen=True)
class PromiseId:
    """
    Identifier for a promise.

    A promise ID consists of an agent ID and an oplog index.
    """

    agent_id: AgentId
    oplog_idx: int

    @classmethod
    def from_string(cls, s: str) -> "PromiseId":
        """
        Parse a promise ID from a string.

        Expected format: `agent-id/oplog-idx`
        """
        parts = s.rsplit("/", 1)
        if len(parts) != 2:
            raise ValueError(f"Invalid promise ID format: {s}")
        agent_id = AgentId.from_string(parts[0])
        oplog_idx = int(parts[1])
        return cls(agent_id=agent_id, oplog_idx=oplog_idx)

    def __str__(self) -> str:
        return f"{self.agent_id}/{self.oplog_idx}"


class Promise:
    """
    A promise that can be awaited and completed.

    Promises allow agents to wait for external events or coordinate
    with other agents.

    Example:
        ```python
        # Create a promise
        promise = create_promise()

        # In another context, complete the promise
        complete_promise(promise.id, b"result data")

        # Wait for the promise
        result = await await_promise(promise.id)
        ```
    """

    def __init__(self, promise_id: PromiseId) -> None:
        self._id = promise_id
        self._result: Optional[bytes] = None
        self._completed = asyncio.Event()

    @property
    def id(self) -> PromiseId:
        """Get the promise ID."""
        return self._id

    @property
    def is_completed(self) -> bool:
        """Check if the promise has been completed."""
        return self._result is not None

    def _complete(self, data: bytes) -> None:
        """Complete the promise with the given data."""
        self._result = data
        self._completed.set()

    async def get(self) -> bytes:
        """Wait for and get the promise result."""
        await self._completed.wait()
        assert self._result is not None
        return self._result


# Promise registry (in runtime, this would interface with the Golem host)
_promises: dict[str, Promise] = {}


def create_promise() -> Promise:
    """
    Create a new promise.

    The promise can be awaited and will block the agent until
    it is completed.

    Returns:
        Promise: A new promise instance.
    """
    # In runtime, this would call the Golem host to create a promise
    # For now, we create a placeholder
    from golem_sdk.agentic.agent_id import ComponentId
    import uuid

    agent_id = AgentId(
        component_id=ComponentId(uuid.uuid4()),
        agent_name="current-agent",
    )
    promise_id = PromiseId(agent_id=agent_id, oplog_idx=len(_promises))
    promise = Promise(promise_id)
    _promises[str(promise_id)] = promise
    return promise


async def await_promise(promise_id: PromiseId) -> bytes:
    """
    Await a promise and return its result.

    This will suspend the agent until the promise is completed.

    Args:
        promise_id: The ID of the promise to await.

    Returns:
        bytes: The data the promise was completed with.
    """
    promise = _promises.get(str(promise_id))
    if promise is None:
        raise ValueError(f"Unknown promise: {promise_id}")
    return await promise.get()


def complete_promise(promise_id: PromiseId, data: bytes) -> None:
    """
    Complete a promise with the given data.

    Args:
        promise_id: The ID of the promise to complete.
        data: The data to complete the promise with.
    """
    promise = _promises.get(str(promise_id))
    if promise is None:
        raise ValueError(f"Unknown promise: {promise_id}")
    promise._complete(data)


def blocking_await_promise(promise_id: PromiseId) -> bytes:
    """
    Synchronously wait for a promise.

    This blocks the current thread until the promise is completed.
    Use `await_promise` for async code.

    Args:
        promise_id: The ID of the promise to await.

    Returns:
        bytes: The data the promise was completed with.
    """
    return asyncio.get_event_loop().run_until_complete(await_promise(promise_id))
