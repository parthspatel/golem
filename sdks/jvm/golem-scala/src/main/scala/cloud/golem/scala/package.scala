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
 * Scala SDK for building Golem applications.
 *
 * This package provides Scala-idiomatic wrappers around the Java Golem SDK.
 *
 * == Usage ==
 *
 * Import the types package for implicit conversions:
 * {{{
 * import cloud.golem.scala.types.conversions.given
 * }}}
 *
 * Use annotations from the agentic package:
 * {{{
 * import cloud.golem.scala.agentic.*
 *
 * @agent
 * class MyAgent extends BaseAgent:
 *   @prompt("Do something")
 *   def doSomething(): String = "done"
 * }}}
 *
 * Use the transaction API:
 * {{{
 * import cloud.golem.scala.host.Transaction
 *
 * Transaction.atomically {
 *   // atomic operations
 * }
 * }}}
 */
package object scala:

  /** Re-export Java base types for convenience. */
  type BaseAgent = cloud.golem.java.agentic.BaseAgent
  type AgentId = cloud.golem.java.agentic.AgentId
  type ComponentId = cloud.golem.java.agentic.ComponentId

  /** Re-export Java Option/Result/Either for interop. */
  type JOption[T] = cloud.golem.java.types.Option[T]
  type JResult[T, E] = cloud.golem.java.types.Result[T, E]
  type JEither[L, R] = cloud.golem.java.types.Either[L, R]

  /** Factory methods for Java types. */
  object JOption:
    def some[T](value: T): cloud.golem.java.types.Option[T] =
      cloud.golem.java.types.Option.some(value)
    def none[T](): cloud.golem.java.types.Option[T] =
      cloud.golem.java.types.Option.none()

  object JResult:
    def ok[T, E](value: T): cloud.golem.java.types.Result[T, E] =
      cloud.golem.java.types.Result.ok(value)
    def err[T, E](error: E): cloud.golem.java.types.Result[T, E] =
      cloud.golem.java.types.Result.err(error)

  object JEither:
    def left[L, R](value: L): cloud.golem.java.types.Either[L, R] =
      cloud.golem.java.types.Either.left(value)
    def right[L, R](value: R): cloud.golem.java.types.Either[L, R] =
      cloud.golem.java.types.Either.right(value)
