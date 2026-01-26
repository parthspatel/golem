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

import org.teavm.interop.Import;

/**
 * A pollable resource that can be blocked on.
 *
 * <p>This corresponds to the WASI pollable type and is used for
 * async operations like waiting for promises.
 */
public final class Pollable {

    private final int handle;

    Pollable(int handle) {
        this.handle = handle;
    }

    /**
     * Blocks until the pollable is ready.
     * WIT: wasi:io/poll@0.2.0#[method]pollable.block
     */
    @Import(module = "wasi:io/poll@0.2.0", name = "[method]pollable.block")
    private static native void blockRaw(int handle);

    /**
     * Blocks until this pollable is ready.
     */
    public void block() {
        blockRaw(handle);
    }

    /**
     * Checks if the pollable is ready without blocking.
     * WIT: wasi:io/poll@0.2.0#[method]pollable.ready
     */
    @Import(module = "wasi:io/poll@0.2.0", name = "[method]pollable.ready")
    private static native int readyRaw(int handle);

    /**
     * Checks if this pollable is ready without blocking.
     *
     * @return true if ready, false otherwise
     */
    public boolean ready() {
        return readyRaw(handle) != 0;
    }
}
