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

package cloud.golem.zio

import cloud.golem.zio.types.*
import zio.*
import zio.test.*
import zio.test.Assertion.*

object TypesSpec extends ZIOSpecDefault:

  def spec = suite("Golem ZIO Types")(
    suite("GolemOption")(
      test("some should contain value") {
        val opt = GolemOption.some(42)
        assertTrue(opt.isSome) &&
        assertTrue(!opt.isNone) &&
        assertTrue(opt.getOrElse(0) == 42)
      },
      test("none should be empty") {
        val opt: GolemOption[Int] = GolemOption.none
        assertTrue(opt.isNone) &&
        assertTrue(!opt.isSome) &&
        assertTrue(opt.getOrElse(0) == 0)
      },
      test("map should transform value") {
        val opt = GolemOption.some(21)
        val doubled = opt.map(_ * 2)
        assertTrue(doubled.getOrElse(0) == 42)
      },
      test("flatMap should chain operations") {
        val opt = GolemOption.some(21)
        val result = opt.flatMap(x => GolemOption.some(x * 2))
        assertTrue(result.getOrElse(0) == 42)
      },
      test("toZIO should succeed for some") {
        for
          value <- GolemOption.some(42).toZIO
        yield assertTrue(value == 42)
      },
      test("toZIO should fail for none") {
        val effect = GolemOption.none[Int].toZIO
        assertZIO(effect.exit)(fails(isSubtype[NoSuchElementException](anything)))
      }
    ),
    suite("GolemResult")(
      test("ok should contain success value") {
        val result = GolemResult.ok(42)
        assertTrue(result.isOk) &&
        assertTrue(!result.isErr) &&
        assertTrue(result.getOrElse(0) == 42)
      },
      test("err should contain error value") {
        val result: GolemResult[String, Int] = GolemResult.err("error")
        assertTrue(result.isErr) &&
        assertTrue(!result.isOk) &&
        assertTrue(result.getOrElse(0) == 0)
      },
      test("map should transform success value") {
        val result = GolemResult.ok(21)
        val doubled = result.map(_ * 2)
        assertTrue(doubled.getOrElse(0) == 42)
      },
      test("mapError should transform error value") {
        val result: GolemResult[String, Int] = GolemResult.err("error")
        val mapped = result.mapError(_.toUpperCase)
        assertTrue(mapped.fold(identity)(_ => "wrong") == "ERROR")
      },
      test("toZIO should succeed for ok") {
        for
          value <- GolemResult.ok(42).toZIO
        yield assertTrue(value == 42)
      },
      test("toZIO should fail for err") {
        val effect = GolemResult.err[String, Int]("error").toZIO
        assertZIO(effect.exit)(fails(equalTo("error")))
      }
    ),
    suite("GolemEither")(
      test("right should contain right value") {
        val either = GolemEither.right(42)
        assertTrue(either.isRight) &&
        assertTrue(!either.isLeft) &&
        assertTrue(either.fold(_ => 0)(identity) == 42)
      },
      test("left should contain left value") {
        val either: GolemEither[String, Int] = GolemEither.left("error")
        assertTrue(either.isLeft) &&
        assertTrue(!either.isRight) &&
        assertTrue(either.fold(identity)(_ => "wrong") == "error")
      },
      test("map should transform right value") {
        val either = GolemEither.right(21)
        val doubled = either.map(_ * 2)
        assertTrue(doubled.fold(_ => 0)(identity) == 42)
      },
      test("swap should exchange left and right") {
        val either: GolemEither[String, Int] = GolemEither.right(42)
        val swapped = either.swap
        assertTrue(swapped.isLeft) &&
        assertTrue(swapped.fold(identity)(_ => 0) == 42)
      }
    )
  )
