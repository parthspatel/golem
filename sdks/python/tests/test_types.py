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

"""Tests for the types module."""

import pytest

from golem_sdk.types import Option, Result, Either, DataValue


class TestOption:
    def test_some(self):
        opt = Option.some(42)
        assert opt.is_some()
        assert not opt.is_none()
        assert opt.unwrap() == 42

    def test_none(self):
        opt: Option[int] = Option.none()
        assert opt.is_none()
        assert not opt.is_some()
        with pytest.raises(ValueError):
            opt.unwrap()

    def test_unwrap_or(self):
        some_opt = Option.some(42)
        none_opt: Option[int] = Option.none()
        assert some_opt.unwrap_or(0) == 42
        assert none_opt.unwrap_or(0) == 0

    def test_map(self):
        opt = Option.some(21)
        doubled = opt.map(lambda x: x * 2)
        assert doubled.unwrap() == 42

        none_opt: Option[int] = Option.none()
        mapped_none = none_opt.map(lambda x: x * 2)
        assert mapped_none.is_none()

    def test_flat_map(self):
        opt = Option.some(21)
        result = opt.flat_map(lambda x: Option.some(x * 2))
        assert result.unwrap() == 42

    def test_filter(self):
        opt = Option.some(42)
        filtered = opt.filter(lambda x: x > 40)
        assert filtered.unwrap() == 42

        filtered_out = opt.filter(lambda x: x < 40)
        assert filtered_out.is_none()

    def test_equality(self):
        assert Option.some(42) == Option.some(42)
        assert Option.none() == Option.none()
        assert Option.some(42) != Option.some(43)
        assert Option.some(42) != Option.none()


class TestResult:
    def test_ok(self):
        res: Result[int, str] = Result.ok(42)
        assert res.is_ok()
        assert not res.is_err()
        assert res.unwrap() == 42

    def test_err(self):
        res: Result[int, str] = Result.err("error")
        assert res.is_err()
        assert not res.is_ok()
        assert res.unwrap_err() == "error"
        with pytest.raises(ValueError):
            res.unwrap()

    def test_unwrap_or(self):
        ok_res: Result[int, str] = Result.ok(42)
        err_res: Result[int, str] = Result.err("error")
        assert ok_res.unwrap_or(0) == 42
        assert err_res.unwrap_or(0) == 0

    def test_map(self):
        res: Result[int, str] = Result.ok(21)
        doubled = res.map(lambda x: x * 2)
        assert doubled.unwrap() == 42

    def test_map_err(self):
        res: Result[int, str] = Result.err("error")
        mapped = res.map_err(lambda e: e.upper())
        assert mapped.unwrap_err() == "ERROR"

    def test_match(self):
        ok_res: Result[int, str] = Result.ok(42)
        err_res: Result[int, str] = Result.err("error")

        ok_result = ok_res.match(
            ok=lambda x: f"got {x}",
            err=lambda e: f"error: {e}",
        )
        assert ok_result == "got 42"

        err_result = err_res.match(
            ok=lambda x: f"got {x}",
            err=lambda e: f"error: {e}",
        )
        assert err_result == "error: error"


class TestEither:
    def test_left(self):
        either: Either[str, int] = Either.left("error")
        assert either.is_left()
        assert not either.is_right()
        assert either.unwrap_left() == "error"

    def test_right(self):
        either: Either[str, int] = Either.right(42)
        assert either.is_right()
        assert not either.is_left()
        assert either.unwrap_right() == 42

    def test_map(self):
        either: Either[str, int] = Either.right(21)
        doubled = either.map(lambda x: x * 2)
        assert doubled.unwrap_right() == 42

    def test_fold(self):
        left_either: Either[str, int] = Either.left("error")
        right_either: Either[str, int] = Either.right(42)

        left_result = left_either.fold(
            left=lambda s: f"error: {s}",
            right=lambda n: f"got {n}",
        )
        assert left_result == "error: error"

        right_result = right_either.fold(
            left=lambda s: f"error: {s}",
            right=lambda n: f"got {n}",
        )
        assert right_result == "got 42"

    def test_swap(self):
        either: Either[str, int] = Either.right(42)
        swapped = either.swap()
        assert swapped.is_left()
        assert swapped.unwrap_left() == 42


class TestDataValue:
    def test_primitives(self):
        bool_val = DataValue.from_bool(True)
        assert bool_val.to_python() is True

        int_val = DataValue.from_int(42)
        assert int_val.to_python() == 42

        float_val = DataValue.from_float(3.14)
        assert float_val.to_python() == 3.14

        str_val = DataValue.from_string("hello")
        assert str_val.to_python() == "hello"

    def test_list(self):
        list_val = DataValue.list([1, 2, 3])
        assert list_val.to_python() == [1, 2, 3]

    def test_tuple(self):
        tuple_val = DataValue.tuple((1, "hello", True))
        assert tuple_val.to_python() == (1, "hello", True)

    def test_record(self):
        record_val = DataValue.record({"name": "Alice", "age": 30})
        result = record_val.to_python()
        assert result["name"] == "Alice"
        assert result["age"] == 30

    def test_option(self):
        some_val = DataValue.option(42)
        assert some_val.to_python() == 42

        none_val = DataValue.option(None)
        assert none_val.to_python() is None

    def test_from_python(self):
        # Auto-conversion from Python types
        assert DataValue.from_python(42).to_python() == 42
        assert DataValue.from_python("hello").to_python() == "hello"
        assert DataValue.from_python([1, 2]).to_python() == [1, 2]
        assert DataValue.from_python({"a": 1}).to_python() == {"a": 1}
