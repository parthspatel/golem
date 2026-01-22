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

"""Tests for the agent module."""

import pytest
import json

from golem_sdk import BaseAgent, agent, prompt, description, endpoint, AgentId
from golem_sdk.agentic.decorators import get_agent_registry


class TestAgentDecorators:
    def test_agent_decorator_without_args(self):
        @agent
        class SimpleAgent(BaseAgent):
            """A simple test agent."""

            def __init__(self):
                super().__init__()
                self.count = 0

            def increment(self) -> int:
                self.count += 1
                return self.count

        assert hasattr(SimpleAgent, "_golem_agent_metadata")
        metadata = SimpleAgent._golem_agent_metadata
        assert metadata.name == "SimpleAgent"
        assert metadata.description == "A simple test agent."

        # Check it's registered
        assert "SimpleAgent" in get_agent_registry()

    def test_agent_decorator_with_args(self):
        @agent(name="custom-agent", description="Custom description")
        class CustomAgent(BaseAgent):
            pass

        metadata = CustomAgent._golem_agent_metadata
        assert metadata.name == "custom-agent"
        assert metadata.description == "Custom description"

    def test_prompt_decorator(self):
        @agent
        class AgentWithPrompt(BaseAgent):
            @prompt("Add two numbers together")
            def add(self, a: int, b: int) -> int:
                return a + b

        metadata = AgentWithPrompt._golem_agent_metadata
        assert "add" in metadata.methods
        assert metadata.methods["add"].prompt == "Add two numbers together"

    def test_description_decorator(self):
        @agent
        class AgentWithDescription(BaseAgent):
            @description("Returns the current count")
            def get_count(self) -> int:
                return 0

        metadata = AgentWithDescription._golem_agent_metadata
        assert "get_count" in metadata.methods
        assert metadata.methods["get_count"].description == "Returns the current count"

    def test_endpoint_decorator(self):
        @agent
        class AgentWithEndpoint(BaseAgent):
            @endpoint(method="POST", path="/items")
            def create_item(self, name: str) -> dict:
                return {"name": name}

        metadata = AgentWithEndpoint._golem_agent_metadata
        assert "create_item" in metadata.methods
        method_meta = metadata.methods["create_item"]
        assert method_meta.is_endpoint
        assert method_meta.http_method == "POST"
        assert method_meta.http_path == "/items"

    def test_combined_decorators(self):
        @agent(name="full-agent")
        class FullAgent(BaseAgent):
            @prompt("Create a new item")
            @description("Creates an item with the given name")
            @endpoint(method="POST", path="/items")
            def create_item(self, name: str) -> dict:
                return {"name": name}

        metadata = FullAgent._golem_agent_metadata
        method_meta = metadata.methods["create_item"]
        assert method_meta.prompt == "Create a new item"
        assert method_meta.description == "Creates an item with the given name"
        assert method_meta.is_endpoint
        assert method_meta.http_method == "POST"


class TestBaseAgent:
    def test_save_and_load_snapshot(self):
        @agent
        class StatefulAgent(BaseAgent):
            def __init__(self):
                super().__init__()
                self.name = "test"
                self.count = 42

        agent_instance = StatefulAgent()
        snapshot = agent_instance.save_snapshot()

        # Create new instance and load snapshot
        new_instance = StatefulAgent()
        new_instance.name = "different"
        new_instance.count = 0

        new_instance.load_snapshot(snapshot)

        assert new_instance.name == "test"
        assert new_instance.count == 42

    def test_agent_id(self):
        @agent
        class TestAgent(BaseAgent):
            pass

        agent_instance = TestAgent()
        assert agent_instance.agent_id is None

        # Set agent ID
        agent_id = AgentId.from_string(
            "12345678-1234-1234-1234-123456789012/test-agent"
        )
        agent_instance._set_agent_id(agent_id)
        assert agent_instance.agent_id == agent_id


class TestAgentId:
    def test_from_string(self):
        agent_id = AgentId.from_string(
            "12345678-1234-1234-1234-123456789012/my-agent"
        )
        assert str(agent_id.component_id) == "12345678-1234-1234-1234-123456789012"
        assert agent_id.agent_name == "my-agent"
        assert agent_id.phantom_id is None

    def test_from_string_with_phantom_id(self):
        agent_id = AgentId.from_string(
            "12345678-1234-1234-1234-123456789012/my-agent/phantom-123"
        )
        assert agent_id.agent_name == "my-agent"
        assert agent_id.phantom_id == "phantom-123"

    def test_to_string(self):
        agent_id = AgentId.from_string(
            "12345678-1234-1234-1234-123456789012/my-agent"
        )
        assert str(agent_id) == "12345678-1234-1234-1234-123456789012/my-agent"

    def test_invalid_format(self):
        with pytest.raises(ValueError):
            AgentId.from_string("invalid-format")
