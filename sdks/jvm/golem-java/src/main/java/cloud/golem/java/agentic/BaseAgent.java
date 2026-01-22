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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all Golem agents.
 *
 * <p>Agents are the fundamental unit of computation in Golem. They are durable,
 * stateful entities that can process requests and maintain state across
 * invocations.
 *
 * <p>Example:
 * <pre>{@code
 * @Agent
 * public class MyAgent extends BaseAgent {
 *     private int counter = 0;
 *
 *     public int increment() {
 *         counter++;
 *         return counter;
 *     }
 * }
 * }</pre>
 */
public abstract class BaseAgent {

    private AgentId agentId;

    /**
     * Default constructor.
     */
    protected BaseAgent() {
    }

    /**
     * Get the agent's ID if available.
     *
     * @return Optional containing the agent ID, or empty if not set
     */
    public Option<AgentId> getAgentId() {
        return agentId != null ? Option.some(agentId) : Option.none();
    }

    /**
     * Set the agent's ID (called by the runtime).
     *
     * @param agentId The agent ID to set
     */
    protected void setAgentId(AgentId agentId) {
        this.agentId = agentId;
    }

    /**
     * Save the agent's state as a snapshot.
     *
     * <p>Override this method to customize snapshot serialization.
     * By default, this method should be overridden to provide
     * custom serialization logic.
     *
     * @return The serialized agent state
     */
    public byte[] saveSnapshot() {
        // Default implementation - subclasses should override
        return "{}".getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Load the agent's state from a snapshot.
     *
     * <p>Override this method to customize snapshot deserialization.
     *
     * @param data The serialized agent state
     */
    public void loadSnapshot(byte[] data) {
        // Default implementation - subclasses should override
    }
}
