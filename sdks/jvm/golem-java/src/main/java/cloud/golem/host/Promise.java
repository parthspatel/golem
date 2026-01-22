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

/**
 * A promise that can be awaited and completed.
 *
 * <p>Promises allow agents to wait for external events or coordinate
 * with other agents. A promise is created with {@link GolemHost#createPromise()}
 * and can be awaited with {@link #subscribe()} and {@link Pollable#block()}.
 *
 * <p>Example:
 * <pre>{@code
 * PromiseId promiseId = GolemHost.createPromise();
 *
 * // In another worker or via API:
 * GolemHost.completePromise(promiseId, "result".getBytes());
 *
 * // Wait for completion:
 * Promise promise = GolemHost.getPromise(promiseId);
 * promise.subscribe().block();
 * byte[] result = promise.get();
 * }</pre>
 */
public final class Promise {

    private final PromiseHandle handle;

    Promise(PromiseHandle handle) {
        this.handle = handle;
    }

    /**
     * Returns a pollable that can be used to wait for the promise.
     *
     * @return A pollable for this promise
     */
    public Pollable subscribe() {
        return handle.subscribe();
    }

    /**
     * Gets the result of the promise.
     *
     * <p>This should only be called after the promise has been completed,
     * typically after {@code subscribe().block()}.
     *
     * @return The data the promise was completed with, or null if not yet completed
     */
    public byte[] get() {
        return handle.get();
    }
}
