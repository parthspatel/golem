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

"""
Golem Python SDK for building durable, distributed applications.

This SDK provides utilities for building Golem agents, including:
- Agent base classes and decorators
- Type mappings for Golem's type system
- Transaction API for durable operations
- Host API wrappers
"""

from golem_sdk.agentic.base_agent import BaseAgent
from golem_sdk.agentic.decorators import agent, prompt, description, endpoint
from golem_sdk.agentic.agent_id import AgentId
from golem_sdk.types import DataValue, Option, Result, Either

__version__ = "0.0.0"

__all__ = [
    # Agent classes
    "BaseAgent",
    "AgentId",
    # Decorators
    "agent",
    "prompt",
    "description",
    "endpoint",
    # Types
    "DataValue",
    "Option",
    "Result",
    "Either",
    # Version
    "__version__",
]
