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

import java.util.UUID
import zio.json.*

/**
 * Represents a Golem component ID.
 */
final case class ComponentId(value: UUID):
  override def toString: String = value.toString

object ComponentId:
  def fromString(s: String): Either[String, ComponentId] =
    try Right(ComponentId(UUID.fromString(s)))
    catch case e: IllegalArgumentException => Left(s"Invalid UUID: $s")

  def random: ComponentId = ComponentId(UUID.randomUUID())

  given JsonEncoder[ComponentId] = JsonEncoder.string.contramap(_.toString)
  given JsonDecoder[ComponentId] = JsonDecoder.string.mapOrFail(fromString)

/**
 * Represents a Golem agent ID.
 *
 * An agent ID uniquely identifies an agent instance within Golem.
 * It consists of a component ID and an agent name, with an optional
 * phantom ID for disambiguation.
 */
final case class AgentId(
    componentId: ComponentId,
    agentName: String,
    phantomId: Option[String] = None
):
  override def toString: String =
    phantomId match
      case Some(pid) => s"$componentId/$agentName/$pid"
      case None      => s"$componentId/$agentName"

  def parsed: (String, String, Option[String]) =
    (agentName, "", phantomId)

object AgentId:
  /**
   * Parse an agent ID from a string.
   *
   * The expected format is: `component-id/agent-name` or
   * `component-id/agent-name/phantom-id`
   */
  def fromString(s: String): Either[String, AgentId] =
    val parts = s.split("/").toList
    parts match
      case componentStr :: agentName :: rest =>
        ComponentId.fromString(componentStr).map { componentId =>
          val phantomId = rest.headOption
          AgentId(componentId, agentName, phantomId)
        }
      case _ =>
        Left(s"Invalid agent ID format: $s. Expected: component-id/agent-name[/phantom-id]")

  given JsonEncoder[AgentId] = JsonEncoder.string.contramap(_.toString)
  given JsonDecoder[AgentId] = JsonDecoder.string.mapOrFail(fromString)
