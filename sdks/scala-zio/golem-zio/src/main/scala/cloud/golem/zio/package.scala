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

package cloud.golem

/**
 * Golem SDK for Scala with ZIO integration.
 *
 * This package provides ZIO-native APIs for building durable,
 * distributed applications on the Golem platform.
 *
 * == Quick Start ==
 * {{{
 * import cloud.golem.zio._
 * import cloud.golem.zio.agentic._
 * import zio._
 *
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
package object zio:
  // Re-export main types
  export agentic.{BaseAgent, AgentId, ComponentId, agent, prompt, description, endpoint}
  export types.{GolemOption, GolemResult, GolemEither}
  export host.{GolemHost, GolemError, Promise, PromiseId, Transaction, RetryPolicy, PersistenceLevel}
