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

"""Base agent class for Golem agents."""

from abc import ABC
from typing import Any, Dict, Optional, TypeVar, Generic
import json

from golem_sdk.agentic.agent_id import AgentId

T = TypeVar("T")


class BaseAgent(ABC):
    """
    Base class for all Golem agents.

    Agents are the fundamental unit of computation in Golem. They are durable,
    stateful entities that can process requests and maintain state across
    invocations.

    Example:
        ```python
        from golem_sdk import BaseAgent, agent

        @agent
        class MyAgent(BaseAgent):
            def __init__(self, name: str):
                super().__init__()
                self.name = name
                self.counter = 0

            def increment(self) -> int:
                self.counter += 1
                return self.counter
        ```
    """

    _agent_id: Optional[AgentId] = None

    def __init__(self) -> None:
        """Initialize the base agent."""
        pass

    @property
    def agent_id(self) -> Optional[AgentId]:
        """Get the agent's ID if available."""
        return self._agent_id

    def _set_agent_id(self, agent_id: AgentId) -> None:
        """Set the agent's ID (called by the runtime)."""
        self._agent_id = agent_id

    def save_snapshot(self) -> bytes:
        """
        Save the agent's state as a snapshot.

        Override this method to customize snapshot serialization.
        By default, serializes __dict__ to JSON.

        Returns:
            bytes: The serialized agent state.
        """
        state = self._get_serializable_state()
        return json.dumps(state).encode("utf-8")

    def load_snapshot(self, data: bytes) -> None:
        """
        Load the agent's state from a snapshot.

        Override this method to customize snapshot deserialization.
        By default, deserializes JSON to __dict__.

        Args:
            data: The serialized agent state.
        """
        state = json.loads(data.decode("utf-8"))
        self._restore_state(state)

    def _get_serializable_state(self) -> Dict[str, Any]:
        """
        Get the agent's state as a serializable dictionary.

        Override this method to customize which state is serialized.

        Returns:
            Dict[str, Any]: The agent's state.
        """
        return {
            key: value
            for key, value in self.__dict__.items()
            if not key.startswith("_")
        }

    def _restore_state(self, state: Dict[str, Any]) -> None:
        """
        Restore the agent's state from a dictionary.

        Override this method to customize state restoration.

        Args:
            state: The state dictionary to restore from.
        """
        for key, value in state.items():
            if not key.startswith("_"):
                setattr(self, key, value)


class Client(Generic[T]):
    """
    Client for invoking methods on remote agents.

    This class provides a way to call methods on agents running in other
    workers or components.

    Example:
        ```python
        from golem_sdk import Client, AgentId

        # Create a client for a remote agent
        agent_id = AgentId.from_string("component-id/agent-name/instance-id")
        client: Client[MyAgent] = Client(agent_id)

        # Call methods on the remote agent
        result = await client.invoke("increment", [])
        ```
    """

    def __init__(self, agent_id: AgentId) -> None:
        """
        Initialize a client for a remote agent.

        Args:
            agent_id: The ID of the target agent.
        """
        self._agent_id = agent_id

    @property
    def agent_id(self) -> AgentId:
        """Get the target agent's ID."""
        return self._agent_id

    async def invoke(self, method_name: str, args: list[Any]) -> Any:
        """
        Invoke a method on the remote agent.

        Args:
            method_name: The name of the method to invoke.
            args: The arguments to pass to the method.

        Returns:
            The result of the method invocation.
        """
        # This will be implemented by the WIT bindings at runtime
        raise NotImplementedError(
            "Client.invoke is implemented by the Golem runtime"
        )
