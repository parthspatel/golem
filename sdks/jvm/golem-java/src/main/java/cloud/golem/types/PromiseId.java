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

/**
 * Identifier for a promise.
 *
 * <p>A promise ID consists of a worker ID and an oplog index that uniquely
 * identifies the promise within the Golem system.
 */
public final class PromiseId {

    private final WorkerId workerId;
    private final long oplogIdx;

    public PromiseId(WorkerId workerId, long oplogIdx) {
        this.workerId = Objects.requireNonNull(workerId, "workerId");
        this.oplogIdx = oplogIdx;
    }

    /**
     * Creates a PromiseId from a raw value returned by the host.
     */
    public static PromiseId fromRaw(long raw) {
        // In actual implementation, would decode the worker ID and oplog index
        // For now, create a placeholder
        return new PromiseId(WorkerId.placeholder(), raw);
    }

    public WorkerId getWorkerId() {
        return workerId;
    }

    public long getOplogIdx() {
        return oplogIdx;
    }

    /**
     * Gets the high bits for the worker ID portion (for WASM calls).
     */
    public long getWorkerIdHigh() {
        return workerId.getComponentIdHigh();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PromiseId promiseId = (PromiseId) o;
        return oplogIdx == promiseId.oplogIdx && Objects.equals(workerId, promiseId.workerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workerId, oplogIdx);
    }

    @Override
    public String toString() {
        return workerId + "/" + oplogIdx;
    }
}
