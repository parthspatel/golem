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

"""Agent ID utilities."""

from dataclasses import dataclass
from typing import Optional, Tuple
import uuid


@dataclass(frozen=True)
class ComponentId:
    """Represents a Golem component ID."""

    value: uuid.UUID

    @classmethod
    def from_string(cls, s: str) -> "ComponentId":
        """Parse a component ID from a string."""
        return cls(value=uuid.UUID(s))

    def __str__(self) -> str:
        return str(self.value)


@dataclass(frozen=True)
class AgentId:
    """
    Represents a Golem agent ID.

    An agent ID uniquely identifies an agent instance within Golem.
    It consists of a component ID and an agent name, with an optional
    phantom ID for disambiguation.
    """

    component_id: ComponentId
    agent_name: str
    phantom_id: Optional[str] = None

    @classmethod
    def from_string(cls, s: str) -> "AgentId":
        """
        Parse an agent ID from a string.

        The expected format is: `component-id/agent-name` or
        `component-id/agent-name/phantom-id`

        Args:
            s: The string representation of the agent ID.

        Returns:
            AgentId: The parsed agent ID.

        Raises:
            ValueError: If the string format is invalid.
        """
        parts = s.split("/")
        if len(parts) < 2:
            raise ValueError(
                f"Invalid agent ID format: {s}. "
                "Expected: component-id/agent-name[/phantom-id]"
            )

        component_id = ComponentId.from_string(parts[0])
        agent_name = parts[1]
        phantom_id = parts[2] if len(parts) > 2 else None

        return cls(
            component_id=component_id,
            agent_name=agent_name,
            phantom_id=phantom_id,
        )

    def parsed(self) -> Tuple[str, str, Optional[str]]:
        """
        Get the parsed components of the agent ID.

        Returns:
            A tuple of (agent_type_name, agent_parameters, phantom_id).
        """
        return (self.agent_name, "", self.phantom_id)

    def __str__(self) -> str:
        base = f"{self.component_id}/{self.agent_name}"
        if self.phantom_id:
            return f"{base}/{self.phantom_id}"
        return base
