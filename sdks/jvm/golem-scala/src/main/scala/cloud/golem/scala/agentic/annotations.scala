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

package cloud.golem.scala.agentic

import scala.annotation.StaticAnnotation

/**
 * Marks a class as a Golem agent.
 *
 * Agents are the primary unit of computation in Golem. Each agent instance
 * maintains durable state and can be invoked remotely.
 *
 * Example:
 * {{{
 * @agent
 * class CounterAgent extends BaseAgent:
 *   private var count = 0
 *
 *   def increment(): Int =
 *     count += 1
 *     count
 * }}}
 *
 * @param name Optional custom name for the agent. Defaults to the class name.
 * @param description Optional description of the agent's purpose.
 */
class agent(
    name: String = "",
    description: String = ""
) extends StaticAnnotation

/**
 * Adds a prompt to an agent method for AI/LLM integration.
 *
 * The prompt is used by AI systems to understand how to use the method.
 *
 * Example:
 * {{{
 * @prompt("Calculate the sum of two numbers")
 * def add(a: Int, b: Int): Int = a + b
 * }}}
 *
 * @param text The prompt text describing the method's purpose.
 */
class prompt(text: String) extends StaticAnnotation

/**
 * Adds a description to an agent method.
 *
 * The description provides documentation that can be used for
 * API generation and tooling.
 *
 * Example:
 * {{{
 * @description("Returns the current count value")
 * def getCount: Int = count
 * }}}
 *
 * @param text The description text.
 */
class description(text: String) extends StaticAnnotation

/**
 * Exposes an agent method as an HTTP endpoint.
 *
 * Example:
 * {{{
 * @endpoint(method = "POST", path = "/items")
 * def createItem(name: String): Item = Item(name)
 * }}}
 *
 * @param method The HTTP method (GET, POST, PUT, DELETE, etc.).
 * @param path The URL path for the endpoint. Defaults to the method name.
 */
class endpoint(
    method: String = "GET",
    path: String = ""
) extends StaticAnnotation
