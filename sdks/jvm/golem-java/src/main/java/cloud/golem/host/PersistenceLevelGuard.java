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

import cloud.golem.types.PersistenceLevel;

/**
 * Guard that restores the original persistence level when closed.
 *
 * <p>Use with try-with-resources:
 * <pre>{@code
 * try (var guard = GolemHost.usePersistenceLevel(PersistenceLevel.PERSIST_IMMEDIATELY)) {
 *     // Operations with immediate persistence
 * }
 * // Original persistence level is restored
 * }</pre>
 */
public final class PersistenceLevelGuard implements AutoCloseable {

    private final PersistenceLevel originalLevel;
    private boolean closed = false;

    PersistenceLevelGuard(PersistenceLevel originalLevel) {
        this.originalLevel = originalLevel;
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            GolemHost.setOplogPersistenceLevel(originalLevel);
        }
    }
}
