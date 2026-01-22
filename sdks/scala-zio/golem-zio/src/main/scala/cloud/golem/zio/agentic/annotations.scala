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

import scala.annotation.StaticAnnotation

/**
 * Marks a class as a Golem agent.
 *
 * Classes annotated with `@agent` will be registered with the Golem runtime
 * and can be instantiated and invoked through the Golem API.
 *
 * Example:
 * {{{
 * @agent(name = "my-counter")
 * class CounterAgent extends BaseAgent {
 *   // ...
 * }
 * }}}
 *
 * @param name The name of the agent. If not specified, the class name will be used.
 * @param description A description of the agent.
 */
final class agent(
    name: String = "",
    description: String = ""
) extends StaticAnnotation

/**
 * Adds a prompt to an agent method.
 *
 * The prompt is used by AI systems to understand how to use the method.
 *
 * Example:
 * {{{
 * @prompt("Calculate the sum of two numbers")
 * def add(a: Int, b: Int): Int = a + b
 * }}}
 *
 * @param value The prompt text describing the method's purpose.
 */
final class prompt(value: String) extends StaticAnnotation

/**
 * Adds a description to an agent method.
 *
 * The description provides documentation for the method.
 *
 * Example:
 * {{{
 * @description("Returns the current count value")
 * def getCount: Int = count
 * }}}
 *
 * @param value The description text.
 */
final class description(value: String) extends StaticAnnotation

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
 * @param path The URL path for the endpoint. Defaults to the method name if not specified.
 */
final class endpoint(
    method: String = "GET",
    path: String = ""
) extends StaticAnnotation
