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

package cloud.golem.scala.types

import cloud.golem.java.types.{Option as JOption, Result as JResult, Either as JEither}
import scala.jdk.FunctionConverters.*

/** Implicit conversions between Java and Scala Golem types. */
object conversions:

  /** Extension methods for Java Option type. */
  extension [T](opt: JOption[T])
    /** Convert Java Option to Scala Option. */
    def toScala: Option[T] =
      if opt.isDefined then Some(opt.get) else None

    /** Map over the Option value. */
    def mapScala[U](f: T => U): JOption[U] =
      opt.map(f.asJava)

    /** FlatMap over the Option value. */
    def flatMapScala[U](f: T => JOption[U]): JOption[U] =
      opt.flatMap(f.asJava)

  /** Extension methods for Java Result type. */
  extension [T, E](result: JResult[T, E])
    /** Convert Java Result to Scala Either. */
    def toScala: Either[E, T] =
      if result.isOk then Right(result.getOk) else Left(result.getErr)

    /** Map over the Ok value. */
    def mapScala[U](f: T => U): JResult[U, E] =
      result.map(f.asJava)

    /** Map over the Err value. */
    def mapErrScala[E2](f: E => E2): JResult[T, E2] =
      result.mapErr(f.asJava)

    /** FlatMap over the Ok value. */
    def flatMapScala[U](f: T => JResult[U, E]): JResult[U, E] =
      result.flatMap(f.asJava)

  /** Extension methods for Java Either type. */
  extension [L, R](either: JEither[L, R])
    /** Convert Java Either to Scala Either. */
    def toScala: Either[L, R] =
      if either.isRight then Right(either.getRight) else Left(either.getLeft)

    /** Map over the Right value. */
    def mapScala[R2](f: R => R2): JEither[L, R2] =
      either.map(f.asJava)

    /** Map over the Left value. */
    def mapLeftScala[L2](f: L => L2): JEither[L2, R] =
      either.mapLeft(f.asJava)

    /** FlatMap over the Right value. */
    def flatMapScala[R2](f: R => JEither[L, R2]): JEither[L, R2] =
      either.flatMap(f.asJava)

  /** Extension methods to convert Scala Option to Java Option. */
  extension [T](opt: Option[T])
    /** Convert Scala Option to Java Option. */
    def toJava: JOption[T] =
      opt match
        case Some(value) => JOption.some(value)
        case None        => JOption.none()

  /** Extension methods to convert Scala Either to Java Result. */
  extension [E, T](either: Either[E, T])
    /** Convert Scala Either to Java Result. */
    def toJavaResult: JResult[T, E] =
      either match
        case Right(value) => JResult.ok(value)
        case Left(error)  => JResult.err(error)

    /** Convert Scala Either to Java Either. */
    def toJavaEither: JEither[E, T] =
      either match
        case Right(value) => JEither.right(value)
        case Left(value)  => JEither.left(value)

  /** Implicit conversion from Java Option to Scala Option. */
  given [T]: Conversion[JOption[T], Option[T]] = _.toScala

  /** Implicit conversion from Java Result to Scala Either. */
  given [T, E]: Conversion[JResult[T, E], Either[E, T]] = _.toScala

  /** Implicit conversion from Java Either to Scala Either. */
  given [L, R]: Conversion[JEither[L, R], Either[L, R]] = _.toScala
