"""
Эталоны для JVM-тестов режима «3-фаза», посчитанные кодом restim.

Запуск (из корня generator2):
    PYTHONIOENCODING=utf-8 <restim>/venv/Scripts/python.exe \
        app/src/test/resources/threephase/gen_reference.py <restim> app/src/test/resources/threephase

Пишет calib.csv, math.csv, patterns.csv в UTF-8.
"""
import csv
import os
import sys

import numpy as np

restim_root, out_dir = sys.argv[1], sys.argv[2]
sys.path.insert(0, restim_root)

from stim_math.threephase import (  # noqa: E402
    ThreePhaseHardwareCalibration,
    ThreePhaseSignalGenerator,
    intensity_ratio_to_ud_lr,
)
from stim_math.transforms import (  # noqa: E402
    ab_transform,
    ab_transform_inv,
    potential_to_channel_matrix,
    potential_to_channel_matrix_inv,
)
from qt_ui.patterns.threephase.base import get_registered_patterns  # noqa: E402
import qt_ui.patterns.threephase  # noqa: E402,F401  регистрирует паттерны

CALIBRATIONS = [
    (0.0, 0.0, 0.0),
    (0.0, -16.73, -25.62),
    (0.0, -2.83, -3.18),
    (-6.0, 0.0, -3.0),
    (0.0, -40.0, -40.0),
    (-10.0, -10.0, -10.0),
]
POINTS = [
    (1.0, 0.0),
    (-1.0, 0.0),
    (-0.5, 0.8660254),
    (-0.5, -0.8660254),
    (0.0, 0.0),
    (0.3, 0.4),
    (1.2, 0.5),
]
THETAS = [0.0, 0.7, 2.1, 4.0]


def ud_lr(a_db, b_db, c_db):
    n, r = intensity_ratio_to_ud_lr(10 ** (a_db / 10), 10 ** (b_db / 10), 10 ** (c_db / 10))
    return float(n), float(r)


def channel_matrix(n, r):
    hw = ThreePhaseHardwareCalibration(n, r)
    tr = hw.generate_transform_in_ab()
    p, ab = potential_to_channel_matrix, ab_transform
    c = (p @ ab @ tr @ ab_transform_inv @ potential_to_channel_matrix_inv) * hw.scaling_contant(tr)
    t = (p @ ab)[:2, :2] / np.sqrt(3)
    return c[:2, :2] @ t


def write(name, header, rows):
    with open(os.path.join(out_dir, name), "w", encoding="utf-8", newline="") as f:
        w = csv.writer(f, lineterminator="\n")
        w.writerow(header)
        for row in rows:
            w.writerow([x if isinstance(x, str) else repr(float(x)) for x in row])


calib_rows = []
math_rows = []
for a, b, c in CALIBRATIONS:
    n, r = ud_lr(a, b, c)
    k = channel_matrix(n, r)
    calib_rows.append((a, b, c, n, r, k[0, 0], k[0, 1], k[1, 0], k[1, 1]))
    hw = ThreePhaseHardwareCalibration(n, r)
    for alpha, beta in POINTS:
        for theta in THETAS:
            left, right = ThreePhaseSignalGenerator.generate(
                np.array([theta]), np.array([alpha]), np.array([beta]))
            left, right = hw.apply_transform(left, right)
            math_rows.append((a, b, c, alpha, beta, theta, left[0], right[0]))

write("calib.csv", ["aDb", "bDb", "cDb", "neutralDb", "rightDb", "k00", "k01", "k10", "k11"], calib_rows)
write("math.csv", ["aDb", "bDb", "cDb", "alpha", "beta", "theta", "L", "R"], math_rows)

pattern_rows = []
for name, cls in sorted(get_registered_patterns().items()):
    if name == "Mouse":
        continue
    pattern = cls()
    for step in range(1, 601):
        alpha, beta = pattern.update(1 / 60)
        pattern_rows.append((name, str(step), alpha, beta))

write("patterns.csv", ["name", "step", "alpha", "beta"], pattern_rows)
