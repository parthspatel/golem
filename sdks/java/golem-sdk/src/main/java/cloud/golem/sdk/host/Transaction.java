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

package cloud.golem.sdk.host;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Transaction API for durable, atomic operations.
 *
 * <p>Transactions ensure that a group of operations either all succeed
 * or all fail together. In case of failure during replay, the entire
 * transaction is re-executed.
 *
 * <p>Example:
 * <pre>{@code
 * Transaction.atomically(() -> {
 *     externalCall1();
 *     externalCall2();
 * });
 * }</pre>
 */
public final class Transaction implements AutoCloseable {

    private final List<Runnable> compensations = new ArrayList<>();
    private boolean committed = false;

    /**
     * Begin a new transaction.
     *
     * @return A new Transaction instance
     */
    public static Transaction begin() {
        Transaction tx = new Transaction();
        // In runtime, this would call mark_begin_operation()
        return tx;
    }

    /**
     * Execute a block atomically.
     *
     * <p>All operations within the block are treated as a single atomic unit.
     *
     * @param block The block to execute
     * @param <T> The return type
     * @return The result of the block
     */
    public static <T> T atomically(Callable<T> block) {
        try (Transaction tx = begin()) {
            T result = block.call();
            tx.commit();
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Transaction failed", e);
        }
    }

    /**
     * Execute a block atomically (void version).
     *
     * @param block The block to execute
     */
    public static void atomically(Runnable block) {
        try (Transaction tx = begin()) {
            block.run();
            tx.commit();
        }
    }

    /**
     * Add a compensation action to be executed on rollback.
     *
     * @param compensation The compensation action
     */
    public void addCompensation(Runnable compensation) {
        compensations.add(compensation);
    }

    /**
     * Commit the transaction.
     */
    public void commit() {
        // In runtime, this would call mark_end_operation()
        committed = true;
    }

    /**
     * Rollback the transaction by executing compensating actions.
     */
    public void rollback() {
        List<Runnable> reversed = new ArrayList<>(compensations);
        Collections.reverse(reversed);
        for (Runnable compensation : reversed) {
            try {
                compensation.run();
            } catch (Exception e) {
                // Log but continue with other compensations
            }
        }
        compensations.clear();
    }

    @Override
    public void close() {
        if (!committed) {
            rollback();
        }
    }
}
