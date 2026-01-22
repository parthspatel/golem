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

package cloud.golem.java.agentic;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a Golem component ID.
 */
public final class ComponentId {

    private final UUID value;

    /**
     * Creates a new ComponentId.
     *
     * @param value The UUID value
     */
    public ComponentId(UUID value) {
        this.value = Objects.requireNonNull(value, "value must not be null");
    }

    /**
     * Parse a component ID from a string.
     *
     * @param s The string representation of the UUID
     * @return The parsed component ID
     */
    public static ComponentId fromString(String s) {
        return new ComponentId(UUID.fromString(s));
    }

    /**
     * Generate a random component ID.
     *
     * @return A new random component ID
     */
    public static ComponentId random() {
        return new ComponentId(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComponentId that = (ComponentId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
