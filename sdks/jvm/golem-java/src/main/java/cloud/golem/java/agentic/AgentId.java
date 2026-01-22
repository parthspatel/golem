/*
 * Copyright 2024-2025 Golem Cloud
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.golem.java.agentic;

import cloud.golem.java.types.Option;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a Golem agent ID.
 *
 * <p>An agent ID uniquely identifies an agent instance within Golem.
 * It consists of a component ID and an agent name, with an optional
 * phantom ID for disambiguation.
 */
public final class AgentId {

    private final ComponentId componentId;
    private final String agentName;
    private final String phantomId;

    /**
     * Creates a new AgentId.
     *
     * @param componentId The component ID
     * @param agentName The agent name
     * @param phantomId Optional phantom ID (may be null)
     */
    public AgentId(ComponentId componentId, String agentName, String phantomId) {
        this.componentId = Objects.requireNonNull(componentId, "componentId must not be null");
        this.agentName = Objects.requireNonNull(agentName, "agentName must not be null");
        this.phantomId = phantomId;
    }

    /**
     * Creates a new AgentId without a phantom ID.
     *
     * @param componentId The component ID
     * @param agentName The agent name
     */
    public AgentId(ComponentId componentId, String agentName) {
        this(componentId, agentName, null);
    }

    /**
     * Parse an agent ID from a string.
     *
     * <p>The expected format is: {@code component-id/agent-name} or
     * {@code component-id/agent-name/phantom-id}
     *
     * @param s The string representation of the agent ID
     * @return The parsed agent ID
     * @throws IllegalArgumentException If the string format is invalid
     */
    public static AgentId fromString(String s) {
        String[] parts = s.split("/");
        if (parts.length < 2) {
            throw new IllegalArgumentException(
                "Invalid agent ID format: " + s + ". Expected: component-id/agent-name[/phantom-id]"
            );
        }

        ComponentId componentId = ComponentId.fromString(parts[0]);
        String agentName = parts[1];
        String phantomId = parts.length > 2 ? parts[2] : null;

        return new AgentId(componentId, agentName, phantomId);
    }

    public ComponentId getComponentId() {
        return componentId;
    }

    public String getAgentName() {
        return agentName;
    }

    public Option<String> getPhantomId() {
        return phantomId != null ? Option.some(phantomId) : Option.none();
    }

    @Override
    public String toString() {
        if (phantomId != null) {
            return componentId + "/" + agentName + "/" + phantomId;
        }
        return componentId + "/" + agentName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentId agentId = (AgentId) o;
        return Objects.equals(componentId, agentId.componentId) &&
               Objects.equals(agentName, agentId.agentName) &&
               Objects.equals(phantomId, agentId.phantomId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(componentId, agentName, phantomId);
    }
}
