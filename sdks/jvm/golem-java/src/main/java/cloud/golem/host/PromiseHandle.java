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
 * Low-level handle to a promise resource.
 *
 * <p>This class wraps the WASM resource handle for a promise and provides
 * methods to interact with it via the Golem runtime.
 */
final class PromiseHandle {

    private final int handle;

    PromiseHandle(int handle) {
        this.handle = handle;
    }

    /**
     * Creates a pollable for this promise.
     * WIT: [method]future-get-value.subscribe
     */
    @Import(module = "golem:api/host@1.1.0", name = "[method]future-get-value.subscribe")
    private static native int subscribeRaw(int handle);

    Pollable subscribe() {
        int pollableHandle = subscribeRaw(handle);
        return new Pollable(pollableHandle);
    }

    /**
     * Gets the value if available.
     * WIT: [method]future-get-value.get
     */
    @Import(module = "golem:api/host@1.1.0", name = "[method]future-get-value.get")
    private static native int getRaw(int handle, int resultPtr);

    byte[] get() {
        // In actual implementation, would read from WASM memory
        // For now, throw unsupported
        throw new UnsupportedOperationException("Requires WASM runtime");
    }
}
