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

package cloud.golem.java;

import cloud.golem.java.types.Either;
import cloud.golem.java.types.Option;
import cloud.golem.java.types.Result;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class TypesTest {

    @Test
    void testOptionSome() {
        Option<Integer> opt = Option.some(42);
        assertTrue(opt.isSome());
        assertFalse(opt.isNone());
        assertEquals(42, opt.unwrap());
    }

    @Test
    void testOptionNone() {
        Option<Integer> opt = Option.none();
        assertTrue(opt.isNone());
        assertFalse(opt.isSome());
        assertThrows(NoSuchElementException.class, opt::unwrap);
    }

    @Test
    void testOptionUnwrapOr() {
        assertEquals(42, Option.some(42).unwrapOr(0));
        assertEquals(0, Option.<Integer>none().unwrapOr(0));
    }

    @Test
    void testOptionMap() {
        Option<Integer> opt = Option.some(21);
        Option<Integer> doubled = opt.map(x -> x * 2);
        assertEquals(42, doubled.unwrap());

        Option<Integer> none = Option.none();
        Option<Integer> mappedNone = none.map(x -> x * 2);
        assertTrue(mappedNone.isNone());
    }

    @Test
    void testResultOk() {
        Result<Integer, String> result = Result.ok(42);
        assertTrue(result.isOk());
        assertFalse(result.isErr());
        assertEquals(42, result.unwrap());
    }

    @Test
    void testResultErr() {
        Result<Integer, String> result = Result.err("error");
        assertTrue(result.isErr());
        assertFalse(result.isOk());
        assertEquals("error", result.unwrapErr());
        assertThrows(NoSuchElementException.class, result::unwrap);
    }

    @Test
    void testResultMap() {
        Result<Integer, String> result = Result.ok(21);
        Result<Integer, String> doubled = result.map(x -> x * 2);
        assertEquals(42, doubled.unwrap());
    }

    @Test
    void testResultMatch() {
        Result<Integer, String> ok = Result.ok(42);
        String okResult = ok.match(
            x -> "got " + x,
            e -> "error: " + e
        );
        assertEquals("got 42", okResult);

        Result<Integer, String> err = Result.err("oops");
        String errResult = err.match(
            x -> "got " + x,
            e -> "error: " + e
        );
        assertEquals("error: oops", errResult);
    }

    @Test
    void testEitherRight() {
        Either<String, Integer> either = Either.right(42);
        assertTrue(either.isRight());
        assertFalse(either.isLeft());
        assertEquals(42, either.unwrapRight());
    }

    @Test
    void testEitherLeft() {
        Either<String, Integer> either = Either.left("error");
        assertTrue(either.isLeft());
        assertFalse(either.isRight());
        assertEquals("error", either.unwrapLeft());
    }

    @Test
    void testEitherFold() {
        Either<String, Integer> right = Either.right(42);
        String rightResult = right.fold(
            s -> "error: " + s,
            n -> "got " + n
        );
        assertEquals("got 42", rightResult);

        Either<String, Integer> left = Either.left("oops");
        String leftResult = left.fold(
            s -> "error: " + s,
            n -> "got " + n
        );
        assertEquals("error: oops", leftResult);
    }

    @Test
    void testEitherSwap() {
        Either<String, Integer> either = Either.right(42);
        Either<Integer, String> swapped = either.swap();
        assertTrue(swapped.isLeft());
        assertEquals(42, swapped.unwrapLeft());
    }
}
