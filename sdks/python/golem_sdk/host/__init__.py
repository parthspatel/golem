# Copyright 2024-2025 Golem Cloud
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""Host API wrappers for Golem runtime."""

from golem_sdk.host.transaction import (
    transaction,
    atomically,
    Transaction,
    fallible_transaction,
)
from golem_sdk.host.guard import (
    use_persistence_level,
    use_idempotence_mode,
    use_retry_policy,
    PersistenceLevel,
    RetryPolicy,
)
from golem_sdk.host.promise import (
    Promise,
    PromiseId,
    create_promise,
    await_promise,
    complete_promise,
)

__all__ = [
    # Transaction
    "transaction",
    "atomically",
    "Transaction",
    "fallible_transaction",
    # Guards
    "use_persistence_level",
    "use_idempotence_mode",
    "use_retry_policy",
    "PersistenceLevel",
    "RetryPolicy",
    # Promise
    "Promise",
    "PromiseId",
    "create_promise",
    "await_promise",
    "complete_promise",
]
