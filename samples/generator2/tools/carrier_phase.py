#!/usr/bin/env python3
"""
Правка таблиц несущих в app/src/main/assets/Carrier.

Формат файла: 1024 значения uint16 little-endian, диапазон 0..4095,
середина шкалы 2048. Один файл — один период волны, поэтому 1024 отсчёта
это 360 градусов: 256 отсчётов = 90 град, 128 = 45 град.

Операции:
  invert  — отражение по амплитуде, v -> 4095 - v
  shift   — циклический сдвиг влево (форма приходит раньше);
            для сдвига вправо задать отрицательное число

Порядок: сначала инверсия, потом сдвиг.

Примеры:
  python tools/carrier_phase.py --show
  python tools/carrier_phase.py Dnramp --invert --shift 256
  python tools/carrier_phase.py HWave2 --invert --shift -128 --dry-run
"""

import argparse
import struct
import sys
from pathlib import Path

SIZE = 1024
MAX = 4095
MID = 2047.5

CARRIER_DIR = Path(__file__).resolve().parent.parent / "app/src/main/assets/Carrier"


def load(path: Path) -> list[int]:
    raw = path.read_bytes()
    if len(raw) != SIZE * 2:
        sys.exit(f"{path.name}: ожидалось {SIZE * 2} байт, получено {len(raw)}")
    return list(struct.unpack(f"<{SIZE}H", raw))


def save(path: Path, values: list[int]) -> None:
    if len(values) != SIZE or any(not 0 <= v <= MAX for v in values):
        sys.exit(f"{path.name}: таблица испорчена, запись отменена")
    path.write_bytes(struct.pack(f"<{SIZE}H", *values))


def invert(values: list[int]) -> list[int]:
    return [MAX - v for v in values]


def shift(values: list[int], k: int) -> list[int]:
    k %= SIZE
    return values[k:] + values[:k]


def describe(values: list[int]) -> str:
    """Старт, точка перехода через середину вверх, положение пика и минимума"""
    up = [i for i in range(SIZE) if values[i] < MID <= values[(i + 1) % SIZE]]
    return (
        f"старт={values[0]:4}  вверх@{(up[0] + 1) % SIZE if up else '-':>4}  "
        f"пик@{values.index(max(values)):4}  мин@{values.index(min(values)):4}"
    )


def main() -> None:
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("name", nargs="?", help="имя таблицы без .dat, например Dnramp")
    p.add_argument("--invert", action="store_true", help="отразить по амплитуде")
    p.add_argument("--shift", type=int, default=0, help="циклический сдвиг влево в отсчётах")
    p.add_argument("--dry-run", action="store_true", help="показать результат, файл не трогать")
    p.add_argument("--show", action="store_true", help="показать фазу всех таблиц и выйти")
    args = p.parse_args()

    if args.show or not args.name:
        for f in sorted(CARRIER_DIR.glob("*.dat")):
            print(f"{f.stem:10} {describe(load(f))}")
        return

    path = CARRIER_DIR / f"{args.name}.dat"
    if not path.exists():
        sys.exit(f"нет файла {path}")

    before = load(path)
    after = shift(invert(before) if args.invert else list(before), args.shift)

    print(f"{args.name}: инверсия={args.invert} сдвиг={args.shift}")
    print(f"  было : {describe(before)}")
    print(f"  стало: {describe(after)}")

    if args.dry_run:
        print("  (dry-run, файл не изменён)")
        return

    save(path, after)
    print("  записано")


if __name__ == "__main__":
    main()
