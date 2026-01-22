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

package cloud.golem.java.types;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a value that can be one of two types: Left or Right.
 *
 * <p>By convention, Left is used for "failure" or "alternative" cases,
 * while Right is the "success" or "primary" case.
 *
 * @param <L> The left type
 * @param <R> The right type
 */
public final class Either<L, R> {

    private final Object value;
    private final boolean isRight;

    private Either(Object value, boolean isRight) {
        this.value = value;
        this.isRight = isRight;
    }

    /**
     * Create a Left value.
     *
     * @param value The left value
     * @param <L> The left type
     * @param <R> The right type
     * @return An Either containing the left value
     */
    public static <L, R> Either<L, R> left(L value) {
        return new Either<>(value, false);
    }

    /**
     * Create a Right value.
     *
     * @param value The right value
     * @param <L> The left type
     * @param <R> The right type
     * @return An Either containing the right value
     */
    public static <L, R> Either<L, R> right(R value) {
        return new Either<>(value, true);
    }

    /**
     * Check if this is a Left value.
     *
     * @return true if Left
     */
    public boolean isLeft() {
        return !isRight;
    }

    /**
     * Check if this is a Right value.
     *
     * @return true if Right
     */
    public boolean isRight() {
        return isRight;
    }

    /**
     * Get the Left value.
     *
     * @return The Left value
     * @throws NoSuchElementException If this is a Right value
     */
    @SuppressWarnings("unchecked")
    public L unwrapLeft() {
        if (isRight) {
            throw new NoSuchElementException("Called unwrapLeft on a Right: " + value);
        }
        return (L) value;
    }

    /**
     * Get the Right value.
     *
     * @return The Right value
     * @throws NoSuchElementException If this is a Left value
     */
    @SuppressWarnings("unchecked")
    public R unwrapRight() {
        if (!isRight) {
            throw new NoSuchElementException("Called unwrapRight on a Left: " + value);
        }
        return (R) value;
    }

    /**
     * Transform the Right value if present.
     *
     * @param mapper The transformation function
     * @param <A> The result type
     * @return An Either with the transformed Right value
     */
    @SuppressWarnings("unchecked")
    public <A> Either<L, A> map(Function<? super R, ? extends A> mapper) {
        if (isRight) {
            return right(mapper.apply((R) value));
        }
        return left((L) value);
    }

    /**
     * Transform the Left value if present.
     *
     * @param mapper The transformation function
     * @param <A> The result type
     * @return An Either with the transformed Left value
     */
    @SuppressWarnings("unchecked")
    public <A> Either<A, R> mapLeft(Function<? super L, ? extends A> mapper) {
        if (!isRight) {
            return left(mapper.apply((L) value));
        }
        return right((R) value);
    }

    /**
     * Transform the Right value with a function that returns an Either.
     *
     * @param mapper The transformation function
     * @param <A> The result type
     * @return The result of the transformation
     */
    @SuppressWarnings("unchecked")
    public <A> Either<L, A> flatMap(Function<? super R, Either<L, A>> mapper) {
        if (isRight) {
            return mapper.apply((R) value);
        }
        return left((L) value);
    }

    /**
     * Handle both cases and produce a single result.
     *
     * @param leftMapper Function to handle Left case
     * @param rightMapper Function to handle Right case
     * @param <A> The result type
     * @return The result of the matching function
     */
    @SuppressWarnings("unchecked")
    public <A> A fold(
            Function<? super L, ? extends A> leftMapper,
            Function<? super R, ? extends A> rightMapper) {
        if (isRight) {
            return rightMapper.apply((R) value);
        }
        return leftMapper.apply((L) value);
    }

    /**
     * Swap Left and Right.
     *
     * @return An Either with Left and Right swapped
     */
    @SuppressWarnings("unchecked")
    public Either<R, L> swap() {
        if (isRight) {
            return left((R) value);
        }
        return right((L) value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Either<?, ?> either = (Either<?, ?>) o;
        if (isRight != either.isRight) return false;
        return Objects.equals(value, either.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, isRight);
    }

    @Override
    public String toString() {
        return isRight ? "Either.right(" + value + ")" : "Either.left(" + value + ")";
    }
}
