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
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents either a success value (Ok) or an error value (Err).
 *
 * <p>This is similar to Rust's Result type and provides a way to handle
 * operations that may fail without using exceptions.
 *
 * @param <T> The success type
 * @param <E> The error type
 */
public final class Result<T, E> {

    private final Object value;
    private final boolean isOk;

    private Result(Object value, boolean isOk) {
        this.value = value;
        this.isOk = isOk;
    }

    /**
     * Create a successful Result.
     *
     * @param value The success value
     * @param <T> The success type
     * @param <E> The error type
     * @return A successful Result
     */
    public static <T, E> Result<T, E> ok(T value) {
        return new Result<>(value, true);
    }

    /**
     * Create a failed Result.
     *
     * @param error The error value
     * @param <T> The success type
     * @param <E> The error type
     * @return A failed Result
     */
    public static <T, E> Result<T, E> err(E error) {
        return new Result<>(error, false);
    }

    /**
     * Check if the Result is successful.
     *
     * @return true if successful
     */
    public boolean isOk() {
        return isOk;
    }

    /**
     * Check if the Result is an error.
     *
     * @return true if an error
     */
    public boolean isErr() {
        return !isOk;
    }

    /**
     * Get the success value.
     *
     * @return The success value
     * @throws NoSuchElementException If the Result is an error
     */
    @SuppressWarnings("unchecked")
    public T unwrap() {
        if (!isOk) {
            throw new NoSuchElementException("Called unwrap on an Err Result: " + value);
        }
        return (T) value;
    }

    /**
     * Get the error value.
     *
     * @return The error value
     * @throws NoSuchElementException If the Result is successful
     */
    @SuppressWarnings("unchecked")
    public E unwrapErr() {
        if (isOk) {
            throw new NoSuchElementException("Called unwrapErr on an Ok Result: " + value);
        }
        return (E) value;
    }

    /**
     * Get the success value or a default.
     *
     * @param defaultValue The default value
     * @return The success value or the default
     */
    @SuppressWarnings("unchecked")
    public T unwrapOr(T defaultValue) {
        return isOk ? (T) value : defaultValue;
    }

    /**
     * Get the success value or compute a default from the error.
     *
     * @param mapper Function to compute default from error
     * @return The success value or the computed default
     */
    @SuppressWarnings("unchecked")
    public T unwrapOrElse(Function<? super E, ? extends T> mapper) {
        return isOk ? (T) value : mapper.apply((E) value);
    }

    /**
     * Transform the success value if present.
     *
     * @param mapper The transformation function
     * @param <U> The result type
     * @return A Result with the transformed value
     */
    @SuppressWarnings("unchecked")
    public <U> Result<U, E> map(Function<? super T, ? extends U> mapper) {
        if (isOk) {
            return ok(mapper.apply((T) value));
        }
        return err((E) value);
    }

    /**
     * Transform the error value if present.
     *
     * @param mapper The transformation function
     * @param <F> The result error type
     * @return A Result with the transformed error
     */
    @SuppressWarnings("unchecked")
    public <F> Result<T, F> mapErr(Function<? super E, ? extends F> mapper) {
        if (isOk) {
            return ok((T) value);
        }
        return err(mapper.apply((E) value));
    }

    /**
     * Transform the success value with a function that returns a Result.
     *
     * @param mapper The transformation function
     * @param <U> The result type
     * @return The result of the transformation
     */
    @SuppressWarnings("unchecked")
    public <U> Result<U, E> flatMap(Function<? super T, Result<U, E>> mapper) {
        if (isOk) {
            return mapper.apply((T) value);
        }
        return err((E) value);
    }

    /**
     * Pattern match on the Result, handling both cases.
     *
     * @param okMapper Function to handle success case
     * @param errMapper Function to handle error case
     * @param <U> The result type
     * @return The result of the matching function
     */
    @SuppressWarnings("unchecked")
    public <U> U match(
            Function<? super T, ? extends U> okMapper,
            Function<? super E, ? extends U> errMapper) {
        if (isOk) {
            return okMapper.apply((T) value);
        }
        return errMapper.apply((E) value);
    }

    /**
     * Execute an action if successful.
     *
     * @param action The action to execute
     */
    @SuppressWarnings("unchecked")
    public void ifOk(Consumer<? super T> action) {
        if (isOk) {
            action.accept((T) value);
        }
    }

    /**
     * Execute an action if an error.
     *
     * @param action The action to execute
     */
    @SuppressWarnings("unchecked")
    public void ifErr(Consumer<? super E> action) {
        if (!isOk) {
            action.accept((E) value);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Result<?, ?> result = (Result<?, ?>) o;
        if (isOk != result.isOk) return false;
        return Objects.equals(value, result.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, isOk);
    }

    @Override
    public String toString() {
        return isOk ? "Result.ok(" + value + ")" : "Result.err(" + value + ")";
    }
}
