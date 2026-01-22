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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Provides transactional semantics for Golem operations.
 *
 * <p>Transactions allow grouping multiple operations into an atomic unit.
 * If any operation fails, the entire transaction can be rolled back by
 * executing compensation actions and rewinding the oplog.
 *
 * <h2>Infallible Transactions</h2>
 * <p>For operations that must eventually succeed (with retries):
 * <pre>{@code
 * String result = Transaction.infallible(tx -> {
 *     String a = tx.execute(
 *         () -> externalCall(),           // Operation
 *         r -> undoExternalCall(r)         // Compensation
 *     );
 *     String b = tx.execute(
 *         () -> anotherCall(a),
 *         r -> undoAnotherCall(r)
 *     );
 *     return b;
 * });
 * }</pre>
 *
 * <h2>Fallible Transactions</h2>
 * <p>For operations that may fail and should abort:
 * <pre>{@code
 * Result<String, Error> result = Transaction.fallible(tx -> {
 *     String a = tx.execute(
 *         () -> maybeFailingCall(),
 *         r -> undoCall(r)
 *     );
 *     return Result.ok(a);
 * });
 * }</pre>
 */
public final class Transaction {

    private Transaction() {
        // Use static factory methods
    }

    /**
     * Executes a block of code as an infallible transaction.
     *
     * <p>If any operation fails, the transaction will:
     * <ol>
     *   <li>Execute compensation actions in reverse order</li>
     *   <li>Rewind the oplog to the beginning of the transaction</li>
     *   <li>The Golem runtime will then retry the entire transaction</li>
     * </ol>
     *
     * @param <T> The return type
     * @param block The transaction block
     * @return The result of the transaction
     */
    public static <T> T infallible(TransactionBlock<T, InfallibleTransaction> block) {
        AtomicOperationGuard guard = GolemHost.markBeginOperation();
        OplogIndex beginIndex = GolemHost.getOplogIndex();
        InfallibleTransaction tx = new InfallibleTransaction(beginIndex);

        try {
            T result = block.execute(tx);
            guard.close();
            return result;
        } catch (Exception e) {
            tx.retry();
            // After retry(), set_oplog_index is called, so this code
            // won't actually execute - the worker will restart from beginIndex
            throw new AssertionError("Unreachable after retry");
        }
    }

    /**
     * Executes a block of code as a fallible transaction.
     *
     * <p>If any operation fails, the transaction will:
     * <ol>
     *   <li>Execute compensation actions in reverse order</li>
     *   <li>Return the error to the caller</li>
     * </ol>
     *
     * @param <T> The success type
     * @param <E> The error type
     * @param block The transaction block
     * @return Ok with the result, or Err if the transaction failed
     */
    public static <T, E> Result<T, E> fallible(TransactionBlock<Result<T, E>, FallibleTransaction<E>> block) {
        AtomicOperationGuard guard = GolemHost.markBeginOperation();
        OplogIndex beginIndex = GolemHost.getOplogIndex();
        FallibleTransaction<E> tx = new FallibleTransaction<>(beginIndex);

        try {
            Result<T, E> result = block.execute(tx);
            guard.close();
            return result;
        } catch (TransactionAbortException e) {
            tx.rollback();
            guard.close();
            @SuppressWarnings("unchecked")
            E error = (E) e.getError();
            return Result.err(error);
        } catch (Exception e) {
            tx.rollback();
            guard.close();
            throw e;
        }
    }

    /**
     * Executes a block atomically without transaction semantics.
     *
     * <p>The block is marked as an atomic operation in the oplog,
     * but no compensation or rollback is performed on failure.
     *
     * @param <T> The return type
     * @param block The code to execute
     * @return The result of the block
     */
    public static <T> T atomically(Supplier<T> block) {
        try (AtomicOperationGuard guard = GolemHost.markBeginOperation()) {
            return block.get();
        }
    }

    /**
     * Executes a block atomically without a return value.
     *
     * @param block The code to execute
     */
    public static void atomically(Runnable block) {
        try (AtomicOperationGuard guard = GolemHost.markBeginOperation()) {
            block.run();
        }
    }

    // =========================================================================
    // Transaction Interfaces
    // =========================================================================

    /**
     * Functional interface for transaction blocks.
     */
    @FunctionalInterface
    public interface TransactionBlock<T, TX> {
        T execute(TX transaction) throws Exception;
    }

    /**
     * Functional interface for compensation actions.
     */
    @FunctionalInterface
    public interface Compensation<T> {
        void compensate(T result);
    }

    // =========================================================================
    // Infallible Transaction
    // =========================================================================

    /**
     * A transaction that will always eventually succeed through retries.
     */
    public static final class InfallibleTransaction {

        private final OplogIndex beginIndex;
        private final List<Runnable> compensations = new ArrayList<>();

        InfallibleTransaction(OplogIndex beginIndex) {
            this.beginIndex = beginIndex;
        }

        /**
         * Executes an operation within the transaction.
         *
         * @param <T> The result type
         * @param operation The operation to execute
         * @param compensation The compensation action if rollback is needed
         * @return The result of the operation
         */
        public <T> T execute(Supplier<T> operation, Compensation<T> compensation) {
            T result = operation.get();
            compensations.add(() -> compensation.compensate(result));
            return result;
        }

        /**
         * Executes an operation without compensation.
         *
         * @param <T> The result type
         * @param operation The operation to execute
         * @return The result of the operation
         */
        public <T> T execute(Supplier<T> operation) {
            return operation.get();
        }

        /**
         * Retries the transaction by rolling back and rewinding the oplog.
         */
        void retry() {
            // Execute compensations in reverse order
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

            // Rewind the oplog - the worker will restart from beginIndex
            GolemHost.setOplogIndex(beginIndex);
        }
    }

    // =========================================================================
    // Fallible Transaction
    // =========================================================================

    /**
     * A transaction that may fail and abort with an error.
     */
    public static final class FallibleTransaction<E> {

        private final OplogIndex beginIndex;
        private final List<Runnable> compensations = new ArrayList<>();

        FallibleTransaction(OplogIndex beginIndex) {
            this.beginIndex = beginIndex;
        }

        /**
         * Executes an operation within the transaction.
         *
         * @param <T> The result type
         * @param operation The operation to execute
         * @param compensation The compensation action if rollback is needed
         * @return The result of the operation
         */
        public <T> T execute(Supplier<T> operation, Compensation<T> compensation) {
            T result = operation.get();
            compensations.add(() -> compensation.compensate(result));
            return result;
        }

        /**
         * Aborts the transaction with an error.
         *
         * @param error The error to return
         * @throws TransactionAbortException Always thrown to abort the transaction
         */
        public void abort(E error) {
            throw new TransactionAbortException(error);
        }

        /**
         * Rolls back the transaction by executing compensations.
         */
        void rollback() {
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
    }

    // =========================================================================
    // Result Type (for fallible transactions)
    // =========================================================================

    /**
     * A result that is either Ok or Err.
     */
    public static final class Result<T, E> {
        private final T ok;
        private final E err;
        private final boolean isOk;

        private Result(T ok, E err, boolean isOk) {
            this.ok = ok;
            this.err = err;
            this.isOk = isOk;
        }

        public static <T, E> Result<T, E> ok(T value) {
            return new Result<>(value, null, true);
        }

        public static <T, E> Result<T, E> err(E error) {
            return new Result<>(null, error, false);
        }

        public boolean isOk() {
            return isOk;
        }

        public boolean isErr() {
            return !isOk;
        }

        public T getOk() {
            if (!isOk) throw new IllegalStateException("Result is Err");
            return ok;
        }

        public E getErr() {
            if (isOk) throw new IllegalStateException("Result is Ok");
            return err;
        }
    }

    // =========================================================================
    // Internal Exception
    // =========================================================================

    private static class TransactionAbortException extends RuntimeException {
        private final Object error;

        TransactionAbortException(Object error) {
            this.error = error;
        }

        Object getError() {
            return error;
        }
    }
}
