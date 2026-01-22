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

"""DataValue type for representing Golem's dynamic value type."""

from dataclasses import dataclass, field
from enum import Enum, auto
from typing import Any, Dict, List, Optional, Union


class ValueType(Enum):
    """Enum representing the different value types in Golem."""

    BOOL = auto()
    S8 = auto()
    S16 = auto()
    S32 = auto()
    S64 = auto()
    U8 = auto()
    U16 = auto()
    U32 = auto()
    U64 = auto()
    F32 = auto()
    F64 = auto()
    CHAR = auto()
    STRING = auto()
    LIST = auto()
    TUPLE = auto()
    RECORD = auto()
    VARIANT = auto()
    ENUM = auto()
    FLAGS = auto()
    OPTION = auto()
    RESULT = auto()


@dataclass
class DataValue:
    """
    Represents a dynamic value in Golem's type system.

    DataValue is used for serializing and deserializing values between
    different components and for method invocations.

    Example:
        ```python
        # Create primitive values
        int_val = DataValue.from_int(42)
        str_val = DataValue.from_string("hello")

        # Create complex values
        record_val = DataValue.record({"name": "Alice", "age": 30})
        list_val = DataValue.list([1, 2, 3])

        # Convert back to Python
        value = int_val.to_python()  # 42
        ```
    """

    value_type: ValueType
    value: Any

    @classmethod
    def from_bool(cls, value: bool) -> "DataValue":
        """Create a DataValue from a boolean."""
        return cls(ValueType.BOOL, value)

    @classmethod
    def from_int(cls, value: int, signed: bool = True, bits: int = 64) -> "DataValue":
        """Create a DataValue from an integer."""
        if signed:
            type_map = {8: ValueType.S8, 16: ValueType.S16, 32: ValueType.S32, 64: ValueType.S64}
        else:
            type_map = {8: ValueType.U8, 16: ValueType.U16, 32: ValueType.U32, 64: ValueType.U64}
        return cls(type_map.get(bits, ValueType.S64), value)

    @classmethod
    def from_float(cls, value: float, bits: int = 64) -> "DataValue":
        """Create a DataValue from a float."""
        vtype = ValueType.F32 if bits == 32 else ValueType.F64
        return cls(vtype, value)

    @classmethod
    def from_string(cls, value: str) -> "DataValue":
        """Create a DataValue from a string."""
        return cls(ValueType.STRING, value)

    @classmethod
    def from_char(cls, value: str) -> "DataValue":
        """Create a DataValue from a character."""
        if len(value) != 1:
            raise ValueError("Char must be a single character")
        return cls(ValueType.CHAR, value)

    @classmethod
    def list(cls, values: List[Any]) -> "DataValue":
        """Create a DataValue from a list."""
        return cls(ValueType.LIST, [cls.from_python(v) for v in values])

    @classmethod
    def tuple(cls, values: tuple) -> "DataValue":
        """Create a DataValue from a tuple."""
        return cls(ValueType.TUPLE, [cls.from_python(v) for v in values])

    @classmethod
    def record(cls, fields: Dict[str, Any]) -> "DataValue":
        """Create a DataValue from a record (dict)."""
        return cls(
            ValueType.RECORD,
            {k: cls.from_python(v) for k, v in fields.items()},
        )

    @classmethod
    def variant(cls, name: str, value: Optional[Any] = None) -> "DataValue":
        """Create a DataValue from a variant."""
        return cls(
            ValueType.VARIANT,
            {"name": name, "value": cls.from_python(value) if value is not None else None},
        )

    @classmethod
    def enum(cls, name: str) -> "DataValue":
        """Create a DataValue from an enum."""
        return cls(ValueType.ENUM, name)

    @classmethod
    def flags(cls, flags: List[str]) -> "DataValue":
        """Create a DataValue from flags."""
        return cls(ValueType.FLAGS, flags)

    @classmethod
    def option(cls, value: Optional[Any]) -> "DataValue":
        """Create a DataValue from an optional value."""
        if value is None:
            return cls(ValueType.OPTION, None)
        return cls(ValueType.OPTION, cls.from_python(value))

    @classmethod
    def result(cls, ok: Optional[Any] = None, err: Optional[Any] = None) -> "DataValue":
        """Create a DataValue from a result."""
        if err is not None:
            return cls(ValueType.RESULT, {"tag": "err", "value": cls.from_python(err)})
        return cls(ValueType.RESULT, {"tag": "ok", "value": cls.from_python(ok) if ok is not None else None})

    @classmethod
    def from_python(cls, value: Any) -> "DataValue":
        """Convert a Python value to a DataValue."""
        if isinstance(value, DataValue):
            return value
        if isinstance(value, bool):
            return cls.from_bool(value)
        if isinstance(value, int):
            return cls.from_int(value)
        if isinstance(value, float):
            return cls.from_float(value)
        if isinstance(value, str):
            return cls.from_string(value)
        if isinstance(value, list):
            return cls.list(value)
        if isinstance(value, tuple):
            return cls.tuple(value)
        if isinstance(value, dict):
            return cls.record(value)
        if value is None:
            return cls.option(None)
        raise ValueError(f"Cannot convert {type(value)} to DataValue")

    def to_python(self) -> Any:
        """Convert a DataValue back to a Python value."""
        if self.value_type in (
            ValueType.BOOL,
            ValueType.S8,
            ValueType.S16,
            ValueType.S32,
            ValueType.S64,
            ValueType.U8,
            ValueType.U16,
            ValueType.U32,
            ValueType.U64,
            ValueType.F32,
            ValueType.F64,
            ValueType.CHAR,
            ValueType.STRING,
            ValueType.ENUM,
        ):
            return self.value

        if self.value_type == ValueType.LIST:
            return [v.to_python() for v in self.value]

        if self.value_type == ValueType.TUPLE:
            return tuple(v.to_python() for v in self.value)

        if self.value_type == ValueType.RECORD:
            return {k: v.to_python() for k, v in self.value.items()}

        if self.value_type == ValueType.VARIANT:
            name = self.value["name"]
            val = self.value["value"]
            return (name, val.to_python() if val is not None else None)

        if self.value_type == ValueType.FLAGS:
            return self.value

        if self.value_type == ValueType.OPTION:
            if self.value is None:
                return None
            return self.value.to_python()

        if self.value_type == ValueType.RESULT:
            tag = self.value["tag"]
            val = self.value["value"]
            if tag == "ok":
                return ("ok", val.to_python() if val is not None else None)
            return ("err", val.to_python() if val is not None else None)

        raise ValueError(f"Unknown value type: {self.value_type}")

    def __repr__(self) -> str:
        return f"DataValue({self.value_type.name}, {self.value!r})"
