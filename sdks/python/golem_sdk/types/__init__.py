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

"""Type definitions for Golem's type system."""

from golem_sdk.types.option import Option
from golem_sdk.types.result import Result
from golem_sdk.types.either import Either
from golem_sdk.types.data_value import DataValue, ValueType

__all__ = [
    "Option",
    "Result",
    "Either",
    "DataValue",
    "ValueType",
]
