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

import java.util.Objects;
import java.util.UUID;

/**
 * Identifier for a Golem worker.
 *
 * <p>A worker ID consists of a component ID (UUID) and a worker name.
 */
public final class WorkerId {

    private final UUID componentId;
    private final String workerName;

    public WorkerId(UUID componentId, String workerName) {
        this.componentId = Objects.requireNonNull(componentId, "componentId");
        this.workerName = Objects.requireNonNull(workerName, "workerName");
    }

    /**
     * Creates a placeholder WorkerId (for internal use).
     */
    static WorkerId placeholder() {
        return new WorkerId(new UUID(0, 0), "placeholder");
    }

    public UUID getComponentId() {
        return componentId;
    }

    public String getWorkerName() {
        return workerName;
    }

    /**
     * Gets the high 64 bits of the component ID (for WASM calls).
     */
    public long getComponentIdHigh() {
        return componentId.getMostSignificantBits();
    }

    /**
     * Gets the low 64 bits of the component ID (for WASM calls).
     */
    public long getComponentIdLow() {
        return componentId.getLeastSignificantBits();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkerId workerId = (WorkerId) o;
        return Objects.equals(componentId, workerId.componentId) &&
               Objects.equals(workerName, workerId.workerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(componentId, workerName);
    }

    @Override
    public String toString() {
        return "urn:worker:" + componentId + "/" + workerName;
    }

    /**
     * Parses a WorkerId from its string representation.
     *
     * @param s The string in format "urn:worker:uuid/name"
     * @return The parsed WorkerId
     * @throws IllegalArgumentException if the format is invalid
     */
    public static WorkerId parse(String s) {
        if (!s.startsWith("urn:worker:")) {
            throw new IllegalArgumentException("Invalid worker ID format: " + s);
        }
        String rest = s.substring("urn:worker:".length());
        int slashIdx = rest.indexOf('/');
        if (slashIdx == -1) {
            throw new IllegalArgumentException("Invalid worker ID format: " + s);
        }
        UUID componentId = UUID.fromString(rest.substring(0, slashIdx));
        String workerName = rest.substring(slashIdx + 1);
        return new WorkerId(componentId, workerName);
    }
}
