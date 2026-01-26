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

package cloud.golem.host;

import cloud.golem.types.OplogIndex;
import cloud.golem.types.PersistenceLevel;
import cloud.golem.types.PromiseId;
import cloud.golem.types.RetryPolicy;
import cloud.golem.types.WorkerId;
import org.teavm.interop.Import;
import org.teavm.interop.Address;

/**
 * Low-level bindings to Golem host functions.
 *
 * <p>This class contains the direct WASM imports that call into the Golem runtime.
 * These are generated from WIT interfaces and should not be called directly.
 * Use {@link GolemHost} instead.
 *
 * <p>The {@code @Import} annotations define the WASM module and function names
 * that these methods bind to in the Golem runtime.
 */
final class GolemHostBindings {

    private GolemHostBindings() {
        // Static utility class
    }

    // =========================================================================
    // Promise Functions
    // =========================================================================

    /**
     * Creates a new promise.
     * WIT: golem:api/host@1.1.0#create-promise
     */
    @Import(module = "golem:api/host@1.1.0", name = "create-promise")
    static native long createPromiseRaw();

    static PromiseId createPromise() {
        long raw = createPromiseRaw();
        return PromiseId.fromRaw(raw);
    }

    /**
     * Gets a promise by ID.
     * WIT: golem:api/host@1.1.0#get-promise
     */
    @Import(module = "golem:api/host@1.1.0", name = "get-promise")
    static native int getPromiseRaw(long promiseIdHigh, long promiseIdLow);

    static PromiseHandle getPromise(PromiseId promiseId) {
        int handle = getPromiseRaw(promiseId.getWorkerIdHigh(), promiseId.getOplogIdx());
        return new PromiseHandle(handle);
    }

    /**
     * Completes a promise with data.
     * WIT: golem:api/host@1.1.0#complete-promise
     */
    @Import(module = "golem:api/host@1.1.0", name = "complete-promise")
    static native int completePromiseRaw(long promiseIdHigh, long promiseIdLow, Address dataPtr, int dataLen);

    static boolean completePromise(PromiseId promiseId, byte[] data) {
        // In actual implementation, would marshal data to WASM memory
        // and call the raw function
        throw new UnsupportedOperationException("Requires WASM runtime");
    }

    // =========================================================================
    // Oplog Functions
    // =========================================================================

    /**
     * Gets the current oplog index.
     * WIT: golem:api/host@1.1.0#get-oplog-index
     */
    @Import(module = "golem:api/host@1.1.0", name = "get-oplog-index")
    static native long getOplogIndexRaw();

    static OplogIndex getOplogIndex() {
        return new OplogIndex(getOplogIndexRaw());
    }

    /**
     * Sets the oplog index.
     * WIT: golem:api/host@1.1.0#set-oplog-index
     */
    @Import(module = "golem:api/host@1.1.0", name = "set-oplog-index")
    static native void setOplogIndexRaw(long index);

    static void setOplogIndex(OplogIndex index) {
        setOplogIndexRaw(index.getValue());
    }

    /**
     * Commits the oplog.
     * WIT: golem:api/host@1.1.0#oplog-commit
     */
    @Import(module = "golem:api/host@1.1.0", name = "oplog-commit")
    static native void oplogCommit();

    // =========================================================================
    // Persistence Level Functions
    // =========================================================================

    /**
     * Gets the oplog persistence level.
     * WIT: golem:api/host@1.1.0#get-oplog-persistence-level
     */
    @Import(module = "golem:api/host@1.1.0", name = "get-oplog-persistence-level")
    static native int getOplogPersistenceLevelRaw();

    static PersistenceLevel getOplogPersistenceLevel() {
        return PersistenceLevel.fromOrdinal(getOplogPersistenceLevelRaw());
    }

    /**
     * Sets the oplog persistence level.
     * WIT: golem:api/host@1.1.0#set-oplog-persistence-level
     */
    @Import(module = "golem:api/host@1.1.0", name = "set-oplog-persistence-level")
    static native void setOplogPersistenceLevelRaw(int level);

    static void setOplogPersistenceLevel(PersistenceLevel level) {
        setOplogPersistenceLevelRaw(level.ordinal());
    }

    // =========================================================================
    // Atomic Operation Functions
    // =========================================================================

    /**
     * Marks the beginning of an atomic operation.
     * WIT: golem:api/host@1.1.0#mark-begin-operation
     */
    @Import(module = "golem:api/host@1.1.0", name = "mark-begin-operation")
    static native void markBeginOperation();

    /**
     * Marks the end of an atomic operation.
     * WIT: golem:api/host@1.1.0#mark-end-operation
     */
    @Import(module = "golem:api/host@1.1.0", name = "mark-end-operation")
    static native void markEndOperation();

    // =========================================================================
    // Worker Info Functions
    // =========================================================================

    /**
     * Gets the self worker ID.
     * WIT: golem:api/host@1.1.0#get-self-uri
     */
    @Import(module = "golem:api/host@1.1.0", name = "get-self-uri")
    static native void getSelfUriRaw(Address resultPtr);

    static WorkerId getSelfWorkerId() {
        // In actual implementation, would read from WASM memory
        throw new UnsupportedOperationException("Requires WASM runtime");
    }

    // =========================================================================
    // Retry Policy Functions
    // =========================================================================

    /**
     * Gets the current retry policy.
     * WIT: golem:api/host@1.1.0#get-retry-policy
     */
    @Import(module = "golem:api/host@1.1.0", name = "get-retry-policy")
    static native void getRetryPolicyRaw(Address resultPtr);

    static RetryPolicy getRetryPolicy() {
        // In actual implementation, would read from WASM memory
        throw new UnsupportedOperationException("Requires WASM runtime");
    }

    /**
     * Sets the retry policy.
     * WIT: golem:api/host@1.1.0#set-retry-policy
     */
    @Import(module = "golem:api/host@1.1.0", name = "set-retry-policy")
    static native void setRetryPolicyRaw(
        int maxAttempts,
        long minDelayMs,
        long maxDelayMs,
        int multiplierInt, // Fixed-point multiplier
        int hasMaxJitter,
        int maxJitterInt   // Fixed-point jitter
    );

    static void setRetryPolicy(RetryPolicy policy) {
        setRetryPolicyRaw(
            policy.getMaxAttempts(),
            policy.getMinDelayMs(),
            policy.getMaxDelayMs(),
            (int)(policy.getMultiplier() * 1000),
            policy.getMaxJitterFactor().isPresent() ? 1 : 0,
            policy.getMaxJitterFactor().map(j -> (int)(j * 1000)).orElse(0)
        );
    }
}
