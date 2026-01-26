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

package cloud.golem.types;

/**
 * Controls how operations are persisted to the oplog.
 */
public enum PersistenceLevel {

    /**
     * Operations are persisted immediately and synchronously.
     * Provides the strongest durability guarantee but may impact performance.
     */
    PERSIST_IMMEDIATELY,

    /**
     * Operations are persisted asynchronously in the background.
     * Provides better performance with eventual durability.
     */
    PERSIST_ASYNC,

    /**
     * The runtime decides when to persist based on operation type.
     * This is the default mode.
     */
    SMART;

    /**
     * Creates a PersistenceLevel from its ordinal value.
     *
     * @param ordinal The ordinal value
     * @return The corresponding PersistenceLevel
     * @throws IllegalArgumentException if the ordinal is invalid
     */
    public static PersistenceLevel fromOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= values().length) {
            throw new IllegalArgumentException("Invalid persistence level ordinal: " + ordinal);
        }
        return values()[ordinal];
    }
}
