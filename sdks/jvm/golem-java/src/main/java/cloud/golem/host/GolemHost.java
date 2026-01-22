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

import cloud.golem.types.PromiseId;
import cloud.golem.types.OplogIndex;
import cloud.golem.types.PersistenceLevel;
import cloud.golem.types.RetryPolicy;
import cloud.golem.types.WorkerId;

/**
 * Interface to the Golem host runtime APIs.
 *
 * <p>This class provides access to Golem's durability features including:
 * <ul>
 *   <li>Promise creation and awaiting</li>
 *   <li>Oplog manipulation</li>
 *   <li>Persistence level control</li>
 *   <li>Atomic operation markers</li>
 * </ul>
 *
 * <p>All methods in this class call directly into the Golem runtime via
 * WIT-generated bindings. The actual implementations are in the generated
 * binding classes under {@code cloud.golem.api.bindings}.
 *
 * <p><b>Note:</b> This class can only be used when running as a Golem component.
 * Calling these methods outside the Golem runtime will result in errors.
 */
public final class GolemHost {

    private GolemHost() {
        // Static utility class
    }

    // =========================================================================
    // Promise API
    // =========================================================================

    /**
     * Creates a new promise that can be awaited and completed.
     *
     * <p>Promises allow agents to wait for external events or coordinate
     * with other agents.
     *
     * @return The ID of the newly created promise
     */
    public static PromiseId createPromise() {
        // Calls: golem:api/host.create-promise()
        return GolemHostBindings.createPromise();
    }

    /**
     * Gets a promise handle for an existing promise.
     *
     * @param promiseId The ID of the promise to retrieve
     * @return The promise handle
     */
    public static Promise getPromise(PromiseId promiseId) {
        // Calls: golem:api/host.get-promise(promise-id)
        return new Promise(GolemHostBindings.getPromise(promiseId));
    }

    /**
     * Blocks until the promise is completed and returns its result.
     *
     * <p>This will suspend the worker until the promise is completed
     * by another worker or external call.
     *
     * @param promiseId The ID of the promise to await
     * @return The data the promise was completed with
     */
    public static byte[] awaitPromise(PromiseId promiseId) {
        Promise promise = getPromise(promiseId);
        promise.subscribe().block();
        return promise.get();
    }

    /**
     * Completes a promise with the given data.
     *
     * @param promiseId The ID of the promise to complete
     * @param data The data to complete the promise with
     * @return true if the promise was completed, false if already completed
     */
    public static boolean completePromise(PromiseId promiseId, byte[] data) {
        // Calls: golem:api/host.complete-promise(promise-id, data)
        return GolemHostBindings.completePromise(promiseId, data);
    }

    // =========================================================================
    // Oplog API
    // =========================================================================

    /**
     * Gets the current oplog index.
     *
     * <p>The oplog index represents the current position in the operation log,
     * which is used for durability and recovery.
     *
     * @return The current oplog index
     */
    public static OplogIndex getOplogIndex() {
        // Calls: golem:api/host.get-oplog-index()
        return GolemHostBindings.getOplogIndex();
    }

    /**
     * Sets the oplog index, effectively rewinding execution.
     *
     * <p><b>Warning:</b> This is a low-level API. Use {@link Transaction} for
     * safer transaction handling.
     *
     * @param index The oplog index to set
     */
    public static void setOplogIndex(OplogIndex index) {
        // Calls: golem:api/host.set-oplog-index(oplog-idx)
        GolemHostBindings.setOplogIndex(index);
    }

    /**
     * Commits the oplog, ensuring all operations up to this point are durable.
     */
    public static void oplogCommit() {
        // Calls: golem:api/host.oplog-commit()
        GolemHostBindings.oplogCommit();
    }

    // =========================================================================
    // Persistence Level API
    // =========================================================================

    /**
     * Gets the current oplog persistence level.
     *
     * @return The current persistence level
     */
    public static PersistenceLevel getOplogPersistenceLevel() {
        // Calls: golem:api/host.get-oplog-persistence-level()
        return GolemHostBindings.getOplogPersistenceLevel();
    }

    /**
     * Sets the oplog persistence level.
     *
     * @param level The persistence level to set
     */
    public static void setOplogPersistenceLevel(PersistenceLevel level) {
        // Calls: golem:api/host.set-oplog-persistence-level(level)
        GolemHostBindings.setOplogPersistenceLevel(level);
    }

    /**
     * Temporarily uses a different persistence level for the duration of a block.
     *
     * @param level The persistence level to use
     * @return A guard that restores the original level when closed
     */
    public static PersistenceLevelGuard usePersistenceLevel(PersistenceLevel level) {
        PersistenceLevel original = getOplogPersistenceLevel();
        setOplogPersistenceLevel(level);
        return new PersistenceLevelGuard(original);
    }

    // =========================================================================
    // Atomic Operations API
    // =========================================================================

    /**
     * Marks the beginning of an atomic operation.
     *
     * <p>All operations between {@code markBeginOperation} and
     * {@code markEndOperation} are treated as a single atomic unit.
     *
     * @return A guard that marks the end of the operation when closed
     */
    public static AtomicOperationGuard markBeginOperation() {
        // Calls: golem:api/host.mark-begin-operation()
        GolemHostBindings.markBeginOperation();
        return new AtomicOperationGuard();
    }

    /**
     * Marks the end of an atomic operation.
     *
     * <p>This is typically called via {@link AtomicOperationGuard#close()}.
     */
    static void markEndOperation() {
        // Calls: golem:api/host.mark-end-operation()
        GolemHostBindings.markEndOperation();
    }

    // =========================================================================
    // Worker Info API
    // =========================================================================

    /**
     * Gets the current worker's ID.
     *
     * @return The worker ID
     */
    public static WorkerId getSelfWorkerId() {
        // Calls: golem:api/host.get-self-uri() and parses
        return GolemHostBindings.getSelfWorkerId();
    }

    // =========================================================================
    // Retry API
    // =========================================================================

    /**
     * Gets the current retry policy.
     *
     * @return The current retry policy
     */
    public static RetryPolicy getRetryPolicy() {
        // Calls: golem:api/host.get-retry-policy()
        return GolemHostBindings.getRetryPolicy();
    }

    /**
     * Sets the retry policy.
     *
     * @param policy The retry policy to set
     */
    public static void setRetryPolicy(RetryPolicy policy) {
        // Calls: golem:api/host.set-retry-policy(policy)
        GolemHostBindings.setRetryPolicy(policy);
    }
}
