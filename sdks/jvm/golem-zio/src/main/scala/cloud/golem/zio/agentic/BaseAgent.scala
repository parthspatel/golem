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

package cloud.golem.zio.agentic

import zio.*
import zio.json.*

/**
 * Base trait for all Golem agents with ZIO integration.
 *
 * Agents are the fundamental unit of computation in Golem. They are durable,
 * stateful entities that can process requests and maintain state across
 * invocations.
 *
 * Example:
 * {{{
 * @agent
 * class CounterAgent extends BaseAgent:
 *   private var count: Int = 0
 *
 *   def increment: UIO[Int] = ZIO.succeed {
 *     count += 1
 *     count
 *   }
 * }}}
 */
trait BaseAgent:
  private var _agentId: Option[AgentId] = None

  /**
   * Get the agent's ID if available.
   */
  def agentId: Option[AgentId] = _agentId

  /**
   * Set the agent's ID (called by the runtime).
   */
  protected[agentic] def setAgentId(id: AgentId): Unit =
    _agentId = Some(id)

  /**
   * Save the agent's state as a snapshot.
   *
   * Override this method to customize snapshot serialization.
   *
   * @return A ZIO effect that produces the serialized state
   */
  def saveSnapshot: UIO[Array[Byte]] =
    ZIO.succeed("{}".getBytes("UTF-8"))

  /**
   * Load the agent's state from a snapshot.
   *
   * Override this method to customize snapshot deserialization.
   *
   * @param data The serialized agent state
   * @return A ZIO effect that loads the state
   */
  def loadSnapshot(data: Array[Byte]): UIO[Unit] =
    ZIO.unit
