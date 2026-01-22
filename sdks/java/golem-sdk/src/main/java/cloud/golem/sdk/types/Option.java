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

package cloud.golem.sdk.types;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents an optional value that may or may not be present.
 *
 * <p>This is similar to Java's Optional but provides additional methods
 * for compatibility with Golem's type system.
 *
 * @param <T> The type of the contained value
 */
public final class Option<T> {

    private final T value;
    private final boolean present;

    private Option(T value, boolean present) {
        this.value = value;
        this.present = present;
    }

    /**
     * Create an Option containing a value.
     *
     * @param value The value to contain
     * @param <T> The type of the value
     * @return An Option containing the value
     */
    public static <T> Option<T> some(T value) {
        return new Option<>(Objects.requireNonNull(value), true);
    }

    /**
     * Create an empty Option.
     *
     * @param <T> The type of the absent value
     * @return An empty Option
     */
    @SuppressWarnings("unchecked")
    public static <T> Option<T> none() {
        return new Option<>(null, false);
    }

    /**
     * Create an Option from a nullable value.
     *
     * @param value The value (may be null)
     * @param <T> The type of the value
     * @return Option.some(value) if non-null, Option.none() otherwise
     */
    public static <T> Option<T> fromNullable(T value) {
        return value != null ? some(value) : none();
    }

    /**
     * Create an Option from a Java Optional.
     *
     * @param optional The Java Optional
     * @param <T> The type of the value
     * @return An Option equivalent to the Optional
     */
    public static <T> Option<T> fromOptional(Optional<T> optional) {
        return optional.map(Option::some).orElseGet(Option::none);
    }

    /**
     * Check if the Option contains a value.
     *
     * @return true if a value is present
     */
    public boolean isSome() {
        return present;
    }

    /**
     * Check if the Option is empty.
     *
     * @return true if no value is present
     */
    public boolean isNone() {
        return !present;
    }

    /**
     * Get the contained value.
     *
     * @return The contained value
     * @throws NoSuchElementException If the Option is empty
     */
    public T unwrap() {
        if (!present) {
            throw new NoSuchElementException("Called unwrap on a None Option");
        }
        return value;
    }

    /**
     * Get the contained value or a default.
     *
     * @param defaultValue The default value
     * @return The contained value or the default
     */
    public T unwrapOr(T defaultValue) {
        return present ? value : defaultValue;
    }

    /**
     * Get the contained value or compute a default.
     *
     * @param supplier Supplier for the default value
     * @return The contained value or the computed default
     */
    public T unwrapOrElse(Supplier<T> supplier) {
        return present ? value : supplier.get();
    }

    /**
     * Transform the contained value if present.
     *
     * @param mapper The transformation function
     * @param <U> The result type
     * @return An Option containing the transformed value, or None
     */
    public <U> Option<U> map(Function<? super T, ? extends U> mapper) {
        if (present) {
            return some(mapper.apply(value));
        }
        return none();
    }

    /**
     * Transform the contained value with a function that returns an Option.
     *
     * @param mapper The transformation function
     * @param <U> The result type
     * @return The result of the transformation, or None
     */
    public <U> Option<U> flatMap(Function<? super T, Option<U>> mapper) {
        if (present) {
            return mapper.apply(value);
        }
        return none();
    }

    /**
     * Keep the value only if it satisfies the predicate.
     *
     * @param predicate The predicate to test
     * @return This Option if present and predicate is true, None otherwise
     */
    public Option<T> filter(Predicate<? super T> predicate) {
        if (present && predicate.test(value)) {
            return this;
        }
        return none();
    }

    /**
     * Execute an action if a value is present.
     *
     * @param action The action to execute
     */
    public void ifSome(Consumer<? super T> action) {
        if (present) {
            action.accept(value);
        }
    }

    /**
     * Convert to a Java Optional.
     *
     * @return An equivalent Java Optional
     */
    public Optional<T> toOptional() {
        return present ? Optional.of(value) : Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Option<?> option = (Option<?>) o;
        if (present != option.present) return false;
        return Objects.equals(value, option.value);
    }

    @Override
    public int hashCode() {
        return present ? Objects.hash(value) : 0;
    }

    @Override
    public String toString() {
        return present ? "Option.some(" + value + ")" : "Option.none()";
    }
}
