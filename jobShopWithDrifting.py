"""Minimal jobshop example."""
import collections
from ortools.sat.python import cp_model
import matplotlib.pyplot as plt
import matplotlib.dates as mdates
from collections import defaultdict
import types

consts = types.SimpleNamespace()
consts.MAX_TIME=40.0
# if the task duration is above ALLOW_BRIGE_MOVE_TIME
# the bridge can go else where
consts.ALLOW_BRIGE_MOVE_TIME=180
consts.BRIDGE_MOVE_TIME=40

#the frontiere between the two bridges areas
consts.MIDDLE_MACHINE=15
consts.C33_MACHINE=33
consts.C0_MACHINE=0
consts.C35_MACHINE=35

consts.CUMULS_MACHINE=[consts.MIDDLE_MACHINE,consts.C33_MACHINE,consts.C0_MACHINE,consts.C35_MACHINE]

def main() -> None:
    """Minimal jobshop problem."""
    # Data.
    jobs_data =  [ # task = (machine_id, processing_time,drift).
    [(1, 100, 0), (7, 240, 0), (11, 30, 0), (11, 30, 0), (12, 120, 150), (13, 10, 0), (14, 10, 0), (15, 4500, 0), (16, 10, 0), (17, 10, 0), (19, 10, 0), (19, 20, 0), (20, 10, 0), (20, 30, 0), (24, 300, 120), (25, 10, 0), (29, 10, 0), (29, 30, 0), (32, 600, 800), (37, 1200, 800), (35, 100, 0)], [(1, 100, 0), (2, 300, 200), (3, 180, 0), (4, 300, 0), (5, 10, 0), (6, 10, 0), (12, 135, 150), (13, 10, 0), (14, 10, 0), (15, 4320, 0), (16, 10, 0), (17, 10, 0), (19, 10, 0), (20, 10, 0), (27, 941, 180), (28, 20, 0), (25, 10, 0), (26, 480, 120), (25, 10, 0), (29, 10, 0), (31, 900, 800), (37, 900, 800), (35, 100, 0)], [(1, 100, 0), (2, 920, 200), (4, 480, 0), (5, 30, 0), (5, 30, 0), (6, 30, 0), (6, 30, 0), (12, 180, 150), (13, 10, 0), (14, 10, 0), (15, 3360, 0), (16, 10, 0), (16, 10, 0), (17, 10, 0), (17, 10, 0), (19, 10, 0), (20, 10, 0), (24, 600, 120), (25, 10, 0), (25, 10, 0), (29, 10, 0), (29, 10, 0), (32, 1200, 800), (35, 100, 0)], [(1, 100, 0), (2, 300, 200), (4, 180, 0), (5, 10, 0), (6, 10, 0), (12, 180, 150), (13, 10, 0), (8, 10, 0), (9, 240, 0), (8, 30, 0), (10, 5, 0), (12, 180, 150), (13, 10, 0), (15, 2520, 0), (16, 10, 0), (17, 10, 0), (19, 10, 0), (20, 10, 0), (27, 900, 180), (28, 40, 0), (25, 10, 0), (26, 540, 120), (25, 10, 0), (29, 10, 0), (31, 1200, 800), (35, 100, 0)], [(1, 100, 0), (7, 240, 0), (11, 30, 0), (11, 30, 0), (12, 120, 150), (13, 10, 0), (14, 10, 0), (15, 4500, 0), (16, 10, 0), (17, 10, 0), (19, 10, 0), (19, 20, 0), (20, 10, 0), (20, 30, 0), (24, 300, 120), (25, 10, 0), (29, 10, 0), (29, 30, 0), (32, 600, 800), (37, 1200, 800), (35, 100, 0)], [(1, 100, 0), (2, 720, 200), (4, 480, 0), (5, 30, 0), (5, 30, 0), (6, 30, 0), (6, 30, 0), (12, 180, 150), (13, 10, 0), (14, 10, 0), (15, 3540, 0), (16, 10, 0), (16, 10, 0), (17, 10, 0), (17, 10, 0), (19, 10, 0), (20, 10, 0), (24, 600, 120), (25, 10, 0), (25, 10, 0), (29, 10, 0), (29, 10, 0), (32, 1200, 800), (35, 100, 0)], [(1, 100, 0), (2, 600, 200), (3, 45, 0), (5, 15, 0), (6, 10, 0), (12, 180, 150), (13, 10, 0), (8, 10, 0), (9, 120, 0), (9, 120, 0), (8, 10, 0), (10, 5, 0), (11, 5, 0), (12, 180, 150), (13, 10, 0), (15, 3900, 0), (16, 20, 0), (17, 5, 0), (17, 10, 0), (19, 10, 0), (19, 10, 0), (20, 10, 0), (20, 20, 0), (27, 1200, 180), (28, 10, 0), (28, 20, 0), (25, 10, 0), (26, 540, 120), (25, 10, 0), (29, 10, 0), (31, 780, 800), (37, 1200, 800), (35, 100, 0)], [(1, 100, 0), (7, 300, 0), (10, 10, 0), (11, 10, 0), (12, 90, 150), (13, 5, 0), (14, 5, 0), (15, 1800, 0), (16, 20, 0), (16, 20, 0), (17, 20, 0), (17, 20, 0), (19, 10, 0), (20, 20, 0), (24, 540, 120), (25, 20, 0), (29, 20, 0), (32, 600, 800), (37, 900, 800), (35, 100, 0)], [(1, 100, 0), (2, 300, 200), (4, 900, 0), (5, 15, 0), (6, 15, 0), (12, 60, 150), (13, 10, 0), (14, 10, 0), (15, 2400, 0), (16, 10, 0), (17, 10, 0), (19, 10, 0), (20, 10, 0), (24, 600, 120), (25, 10, 0), (29, 10, 0), (32, 1200, 800), (35, 100, 0)], [(1, 100, 0), (7, 300, 0), (10, 10, 0), (11, 10, 0), (12, 180, 150), (13, 10, 0), (8, 10, 0), (9, 120, 0), (9, 120, 0), (8, 10, 0), (10, 5, 0), (11, 5, 0), (12, 180, 150), (13, 10, 0), (14, 10, 0), (15, 3900, 0), (16, 20, 0), (17, 10, 0), (17, 10, 0), (19, 10, 0), (19, 10, 0), (20, 10, 0), (20, 20, 0), (27, 1200, 180), (28, 10, 0), (25, 10, 0), (26, 420, 120), (25, 10, 0), (29, 10, 0), (31, 780, 800), (37, 900, 800), (35, 100, 0)], [(1, 100, 0), (7, 300, 0), (10, 10, 0), (8, 10, 0), (9, 120, 0), (8, 30, 0), (10, 10, 0), (11, 10, 0), (12, 60, 150), (13, 10, 0), (14, 10, 0), (15, 2880, 0), (16, 10, 0), (17, 10, 0), (19, 10, 0), (21, 60, 0), (19, 10, 0), (20, 10, 0), (24, 600, 120), (25, 10, 0), (29, 10, 0), (32, 900, 800), (35, 100, 0)], [(1, 100, 0), (7, 240, 0), (11, 30, 0), (11, 30, 0), (12, 120, 150), (13, 10, 0), (14, 10, 0), (15, 4500, 0), (16, 10, 0), (17, 10, 0), (19, 10, 0), (19, 20, 0), (20, 10, 0), (20, 30, 0), (24, 300, 120), (25, 10, 0), (29, 10, 0), (29, 30, 0), (32, 600, 800), (37, 1200, 800), (35, 100, 0)], [(1, 100, 0), (2, 300, 200)], [(1, 100, 0), (3, 120, 0), (5, 10, 0)], [(1, 100, 0), (2, 300, 200), (4, 180, 0), (5, 10, 0), (6, 10, 0), (12, 180, 150), (13, 10, 0), (8, 10, 0), (9, 240, 0), (8, 30, 0), (10, 5, 0), (12, 180, 150), (13, 10, 0), (15, 2400, 0), (16, 10, 0), (17, 10, 0), (19, 10, 0), (20, 10, 0), (27, 900, 180), (28, 40, 0), (25, 10, 0), (26, 540, 120), (25, 10, 0), (29, 10, 0), (31, 1200, 800), (35, 100, 0)], [(1, 100, 0), (7, 240, 0), (11, 30, 0), (11, 30, 0), (12, 120, 150), (13, 10, 0), (14, 10, 0), (15, 4500, 0), (16, 10, 0), (17, 10, 0), (19, 10, 0), (19, 20, 0), (20, 10, 0), (20, 30, 0), (24, 300, 120), (25, 10, 0), (29, 10, 0), (29, 30, 0), (32, 600, 800), (37, 1200, 800), (35, 100, 0)], [(1, 100, 0), (2, 600, 200), (3, 45, 0), (5, 15, 0), (6, 10, 0), (12, 180, 150), (13, 10, 0), (8, 10, 0), (9, 120, 0), (9, 120, 0), (8, 10, 0), (10, 5, 0), (11, 5, 0), (12, 180, 150), (13, 10, 0), (15, 3900, 0), (16, 20, 0), (17, 5, 0), (17, 10, 0), (19, 10, 0), (19, 10, 0), (20, 10, 0), (20, 20, 0), (27, 1200, 180), (28, 10, 0), (28, 20, 0), (25, 10, 0), (26, 480, 120), (25, 10, 0), (29, 10, 0), (31, 780, 800), (37, 900, 800), (35, 100, 0)], [(1, 100, 0), (2, 720, 200), (4, 480, 0), (5, 30, 0), (5, 30, 0), (6, 30, 0), (6, 30, 0), (12, 180, 150), (13, 10, 0), (14, 10, 0), (15, 3360, 0), (16, 10, 0), (16, 10, 0), (17, 10, 0), (17, 10, 0), (19, 10, 0), (20, 10, 0), (24, 600, 120), (25, 10, 0), (25, 10, 0), (29, 10, 0), (29, 10, 0), (32, 1200, 800), (35, 100, 0)], [(1, 100, 0), (7, 300, 0), (10, 10, 0), (11, 10, 0), (12, 180, 150), (13, 10, 0), (8, 10, 0), (9, 120, 0), (9, 120, 0), (8, 10, 0), (10, 5, 0), (11, 5, 0), (12, 180, 150), (13, 10, 0), (14, 10, 0), (15, 3900, 0), (16, 20, 0), (17, 10, 0), (17, 10, 0), (19, 10, 0), (19, 10, 0), (20, 10, 0), (20, 20, 0), (27, 1200, 180), (28, 10, 0), (25, 10, 0), (26, 480, 120), (25, 10, 0), (29, 10, 0), (31, 780, 800), (37, 1080, 800), (35, 100, 0)], [(1, 100, 0), (2, 300, 200), (4, 600, 0), (5, 30, 0), (5, 30, 0), (6, 30, 0), (6, 30, 0), (12, 120, 150), (13, 30, 0), (14, 30, 0), (15, 4200, 0), (16, 30, 0), (17, 30, 0), (19, 30, 0), (19, 30, 0), (20, 20, 0), (20, 20, 0), (27, 1200, 180), (28, 30, 0), (25, 10, 0), (26, 600, 120), (25, 10, 0), (29, 10, 0), (31, 900, 800), (37, 600, 800), (35, 100, 0)], [(1, 100, 0), (2, 300, 200), (4, 180, 0), (5, 10, 0), (6, 10, 0), (12, 180, 150), (13, 10, 0), (8, 10, 0), (9, 240, 0), (8, 30, 0), (10, 5, 0), (12, 180, 150), (13, 10, 0), (15, 2400, 0), (16, 10, 0), (17, 10, 0), (19, 10, 0), (20, 10, 0), (27, 900, 180), (28, 40, 0), (25, 10, 0), (26, 420, 120), (25, 10, 0), (29, 10, 0), (31, 1200, 800), (35, 100, 0)], [(1, 100, 0), (7, 240, 0), (11, 30, 0), (11, 30, 0), (12, 120, 150), (13, 10, 0), (14, 10, 0), (15, 4500, 0), (16, 10, 0), (17, 10, 0), (19, 10, 0), (19, 20, 0), (20, 10, 0), (20, 30, 0), (24, 300, 120), (25, 10, 0), (29, 10, 0), (29, 30, 0), (32, 600, 800), (37, 900, 800), (35, 100, 0)], [(1, 100, 0), (7, 300, 0), (10, 5, 0), (11, 10, 0), (12, 180, 150), (13, 10, 0), (15, 3840, 0), (16, 10, 0), (17, 10, 0), (12, 240, 150), (13, 10, 0), (14, 10, 0), (19, 10, 0), (20, 10, 0), (20, 10, 0), (22, 120, 0), (22, 75, 0), (20, 10, 0), (20, 10, 0), (29, 300, 0), (33, 3900, 800), (37, 900, 800), (35, 100, 0)], [(1, 100, 0), (2, 300, 200), (4, 900, 0), (5, 15, 0), (6, 15, 0), (12, 60, 150), (13, 10, 0), (14, 10, 0), (15, 2520, 0), (16, 10, 0), (17, 10, 0), (19, 10, 0), (20, 10, 0), (24, 600, 120), (25, 10, 0), (29, 10, 0), (32, 1230, 800), (35, 100, 0)], [(1, 100, 0), (2, 300, 200), (4, 180, 0), (5, 10, 0), (6, 10, 0), (12, 180, 150), (13, 10, 0), (8, 10, 0), (9, 240, 0), (8, 30, 0), (10, 5, 0), (12, 210, 150), (13, 10, 0), (15, 2430, 0), (16, 10, 0), (17, 10, 0), (19, 10, 0), (20, 10, 0), (27, 900, 180), (28, 40, 0), (25, 10, 0), (26, 540, 120), (25, 10, 0), (29, 10, 0), (31, 1200, 800), (35, 100, 0)], [(1, 100, 0), (7, 240, 0), (11, 30, 0), (11, 30, 0), (12, 120, 150), (13, 10, 0), (14, 10, 0), (15, 4500, 0), (16, 10, 0), (17, 10, 0), (19, 10, 0), (19, 20, 0), (20, 10, 0), (20, 30, 0), (24, 300, 120), (25, 10, 0), (29, 10, 0), (29, 30, 0), (32, 600, 800), (37, 960, 800), (35, 100, 0)], [(1, 100, 0), (2, 300, 200), (4, 300, 0), (5, 10, 0), (6, 10, 0), (12, 180, 150), (13, 10, 0), (8, 10, 0), (9, 240, 0), (8, 30, 0), (10, 5, 0), (12, 180, 150), (13, 10, 0), (15, 2700, 0), (16, 10, 0), (17, 10, 0), (19, 10, 0), (20, 10, 0), (27, 900, 180), (28, 40, 0), (25, 10, 0), (26, 540, 120), (25, 10, 0), (29, 10, 0), (31, 1500, 800), (37, 900, 800), (35, 100, 0)], [(1, 100, 0), (7, 270, 0), (11, 30, 0), (11, 30, 0), (12, 120, 150), (13, 10, 0), (14, 10, 0), (15, 4500, 0), (16, 10, 0), (17, 10, 0), (19, 10, 0), (19, 20, 0), (20, 10, 0), (20, 30, 0), (24, 300, 120), (25, 10, 0), (29, 10, 0), (29, 30, 0), (32, 600, 800), (37, 1200, 800), (35, 100, 0)], [(1, 100, 0), (2, 720, 200), (4, 480, 0), (5, 30, 0), (5, 30, 0), (6, 30, 0), (6, 30, 0), (12, 180, 150), (13, 10, 0), (14, 10, 0), (15, 3360, 0), (16, 10, 0), (16, 10, 0), (17, 10, 0), (17, 10, 0), (19, 10, 0), (20, 10, 0), (24, 600, 120), (25, 10, 0), (25, 10, 0), (29, 10, 0), (29, 10, 0), (32, 1200, 800), (35, 100, 0)], [(1, 100, 0), (2, 720, 200), (4, 480, 0), (5, 30, 0), (5, 30, 0), (6, 30, 0), (6, 30, 0), (12, 180, 150), (13, 10, 0), (14, 10, 0), (15, 4035, 0), (16, 10, 0), (16, 10, 0), (17, 10, 0), (17, 10, 0), (19, 10, 0), (20, 10, 0), (24, 600, 120), (25, 10, 0), (25, 10, 0), (29, 10, 0), (29, 10, 0), (32, 1200, 800), (35, 100, 0)], [(1, 100, 0), (7, 420, 0), (10, 10, 0), (9, 60, 0), (8, 10, 0), (10, 5, 0), (11, 5, 0), (12, 180, 150), (13, 10, 0), (14, 10, 0), (15, 4500, 0), (16, 20, 0), (17, 10, 0), (17, 10, 0), (19, 10, 0), (19, 10, 0), (20, 10, 0), (20, 20, 0), (27, 30, 180), (27, 1200, 180), (28, 10, 0), (25, 10, 0), (26, 360, 120), (25, 10, 0), (29, 10, 0), (31, 780, 800), (37, 900, 800), (35, 100, 0)], [(1, 100, 0), (2, 600, 200), (3, 45, 0), (5, 15, 0), (6, 10, 0), (12, 180, 150), (13, 10, 0), (8, 10, 0), (9, 120, 0), (9, 120, 0), (8, 10, 0), (10, 5, 0), (11, 5, 0), (12, 180, 150), (13, 10, 0), (15, 3900, 0), (16, 20, 0), (17, 5, 0), (17, 10, 0), (19, 10, 0), (19, 10, 0), (20, 10, 0), (20, 20, 0), (27, 1200, 180), (28, 10, 0), (28, 20, 0), (25, 10, 0), (26, 540, 120), (25, 10, 0), (29, 10, 0), (31, 780, 800), (37, 600, 800), (35, 100, 0)], [(1, 100, 0), (2, 300, 200), (4, 960, 0), (5, 15, 0), (5, 30, 0), (6, 15, 0), (6, 30, 0), (12, 180, 150), (13, 5, 0), (14, 5, 0), (15, 4200, 0), (16, 10, 0), (17, 10, 0), (19, 10, 0), (20, 10, 0), (24, 600, 120), (25, 10, 0), (29, 10, 0), (32, 1200, 800), (37, 1200, 800), (35, 100, 0)], [(1, 100, 0), (7, 300, 0), (11, 10, 0), (8, 10, 0), (9, 120, 0), (8, 10, 0), (10, 10, 0), (11, 10, 0), (12, 60, 150), (13, 5, 0), (14, 5, 0), (15, 1500, 0), (16, 10, 0), (16, 10, 0), (17, 10, 0), (17, 10, 0), (19, 10, 0), (20, 10, 0), (24, 480, 120), (25, 10, 0), (29, 10, 0), (32, 600, 800), (35, 100, 0)], [(1, 100, 0), (2, 300, 200), (4, 300, 0), (5, 30, 0), (6, 30, 0), (12, 60, 150), (13, 5, 0), (14, 5, 0), (15, 1500, 0), (16, 10, 0), (17, 10, 0), (19, 10, 0), (20, 10, 0), (24, 600, 120), (25, 10, 0), (29, 10, 0), (32, 600, 800), (35, 100, 0)], [(1, 100, 0), (7, 240, 0), (11, 30, 0), (11, 30, 0), (12, 120, 150), (13, 10, 0), (14, 10, 0), (15, 4500, 0), (16, 10, 0), (17, 10, 0), (19, 10, 0), (19, 20, 0), (20, 10, 0), (20, 30, 0), (24, 300, 120), (25, 10, 0), (29, 10, 0), (29, 30, 0), (32, 600, 800), (37, 1200, 800), (35, 100, 0)], [(1, 100, 0), (7, 300, 0), (10, 10, 0), (8, 30, 0), (9, 20, 0), (8, 30, 0), (10, 10, 0), (11, 10, 0), (12, 180, 150), (12, 120, 150), (13, 10, 0), (14, 10, 0), (15, 750, 0), (16, 10, 0), (17, 10, 0), (19, 10, 0), (20, 10, 0), (24, 210, 120), (25, 10, 0), (29, 10, 0), (32, 600, 800)], [(1, 100, 0), (2, 600, 200), (3, 45, 0), (5, 15, 0), (6, 10, 0), (12, 180, 150), (13, 10, 0), (8, 10, 0), (9, 120, 0), (9, 120, 0), (8, 10, 0), (10, 5, 0), (11, 5, 0), (12, 180, 150), (13, 10, 0), (15, 4200, 0), (16, 20, 0), (17, 5, 0), (17, 10, 0), (19, 10, 0), (19, 10, 0), (20, 10, 0), (20, 20, 0), (27, 1200, 180), (28, 10, 0), (28, 20, 0), (25, 10, 0), (26, 540, 120), (25, 10, 0), (29, 10, 0), (31, 780, 800), (37, 1200, 800), (35, 100, 0)], [(1, 100, 0), (2, 300, 200), (4, 900, 0), (5, 15, 0), (6, 15, 0), (12, 60, 150), (13, 10, 0), (14, 10, 0), (15, 2400, 0), (16, 10, 0), (17, 10, 0), (19, 10, 0), (20, 10, 0), (24, 600, 120), (25, 10, 0), (29, 10, 0), (32, 1200, 800), (35, 100, 0)], [(1, 100, 0), (7, 300, 0), (10, 10, 0), (11, 10, 0), (12, 120, 150), (13, 10, 0), (14, 10, 0), (15, 3600, 0), (16, 10, 0), (16, 10, 0), (17, 10, 0), (17, 30, 0), (19, 10, 0), (19, 30, 0), (18, 600, 0), (19, 30, 0)], [(1, 100, 0), (7, 240, 0), (11, 30, 0), (11, 30, 0), (12, 120, 150), (13, 10, 0), (14, 10, 0), (15, 4500, 0), (16, 10, 0), (17, 10, 0), (19, 10, 0), (19, 20, 0), (20, 10, 0), (20, 30, 0), (24, 300, 120), (25, 10, 0), (29, 10, 0), (29, 30, 0), (32, 600, 800), (37, 1200, 800), (35, 100, 0)], [(1, 100, 0), (2, 300, 200), (4, 600, 0), (5, 10, 0), (6, 10, 0), (12, 60, 150), (13, 10, 0), (14, 10, 0), (15, 4200, 0), (16, 10, 0), (17, 10, 0), (19, 10, 0), (20, 10, 0), (27, 900, 180), (28, 20, 0), (25, 10, 0), (26, 600, 120), (25, 10, 0), (29, 10, 0), (31, 900, 800), (35, 100, 0)], [(1, 100, 0), (7, 300, 0), (10, 10, 0), (11, 10, 0), (12, 60, 150), (13, 5, 0), (14, 5, 0), (15, 3000, 0), (16, 10, 0), (16, 10, 0), (17, 10, 0), (17, 10, 0), (19, 10, 0), (20, 10, 0), (24, 600, 120), (25, 10, 0), (29, 10, 0), (32, 900, 800), (35, 100, 0)], [(1, 100, 0), (2, 300, 200), (4, 300, 0), (5, 10, 0), (6, 10, 0), (12, 180, 150), (13, 10, 0), (8, 10, 0), (9, 240, 0), (8, 30, 0), (10, 5, 0), (12, 180, 150), (13, 10, 0), (15, 2700, 0), (16, 10, 0), (17, 10, 0), (19, 10, 0), (20, 10, 0), (27, 900, 180), (28, 40, 0), (25, 10, 0), (26, 540, 120), (25, 10, 0), (29, 10, 0), (31, 1200, 800), (37, 960, 800), (35, 100, 0)], [(1, 100, 0), (2, 300, 200), (4, 900, 0), (5, 15, 0), (6, 15, 0), (12, 60, 150), (13, 10, 0), (14, 10, 0), (15, 2400, 0), (16, 10, 0), (17, 10, 0), (19, 10, 0), (20, 10, 0), (24, 600, 120), (25, 10, 0), (29, 10, 0), (32, 1200, 800), (35, 100, 0)], [(1, 100, 0), (2, 300, 200), (4, 300, 0), (5, 10, 0), (6, 10, 0), (12, 180, 150), (13, 10, 0), (8, 10, 0), (9, 240, 0), (8, 30, 0), (10, 5, 0), (12, 180, 150), (13, 10, 0), (15, 2700, 0), (16, 10, 0), (17, 10, 0), (19, 10, 0), (20, 10, 0), (27, 900, 180), (28, 40, 0), (25, 10, 0), (26, 540, 120), (25, 10, 0), (29, 10, 0), (31, 1200, 800), (37, 900, 800), (35, 100, 0)]]


    #jobs_data=[[(1, 100, 0), (7, 290, 0), (11, 80, 0), (11, 80, 0), (12, 170, 150), (13, 60, 0), (14, 60, 0), (15, 4550, 0), (16, 60, 0), (17, 60, 0), (19, 60, 0), (19, 70, 0), (20, 60, 0), (20, 80, 0), (24, 350, 120), (25, 60, 0), (29, 60, 0), (29, 80, 0), (32, 650, 800), (37, 1250, 800), (35, 100, 0)], [(1, 100, 0), (2, 350, 200), (3, 230, 0), (4, 350, 0), (5, 60, 0), (6, 60, 0), (12, 185, 150), (13, 60, 0), (14, 60, 0), (15, 4370, 0), (16, 60, 0), (17, 60, 0), (19, 60, 0), (20, 60, 0), (27, 991, 180), (28, 70, 0), (25, 60, 0), (26, 530, 120), (25, 60, 0), (29, 60, 0), (31, 950, 800), (37, 950, 800), (35, 100, 0)], [(1, 100, 0), (2, 970, 200), (4, 530, 0), (5, 80, 0), (5, 80, 0), (6, 80, 0), (6, 80, 0), (12, 230, 150), (13, 60, 0), (14, 60, 0), (15, 3410, 0), (16, 60, 0), (16, 60, 0), (17, 60, 0), (17, 60, 0), (19, 60, 0), (20, 60, 0), (24, 650, 120), (25, 60, 0), (25, 60, 0), (29, 60, 0), (29, 60, 0), (32, 1250, 800), (35, 100, 0)], [(1, 100, 0), (2, 350, 200), (4, 230, 0), (5, 60, 0), (6, 60, 0), (12, 230, 150), (13, 60, 0), (8, 60, 0), (9, 290, 0), (8, 80, 0), (10, 55, 0), (12, 230, 150), (13, 60, 0), (15, 2570, 0), (16, 60, 0), (17, 60, 0), (19, 60, 0), (20, 60, 0), (27, 950, 180), (28, 90, 0), (25, 60, 0), (26, 590, 120), (25, 60, 0), (29, 60, 0), (31, 1250, 800), (35, 100, 0)], [(1, 100, 0), (7, 290, 0), (11, 80, 0), (11, 80, 0), (12, 170, 150), (13, 60, 0), (14, 60, 0), (15, 4550, 0), (16, 60, 0), (17, 60, 0), (19, 60, 0), (19, 70, 0), (20, 60, 0), (20, 80, 0), (24, 350, 120), (25, 60, 0), (29, 60, 0), (29, 80, 0), (32, 650, 800), (37, 1250, 800), (35, 100, 0)], [(1, 100, 0), (2, 770, 200), (4, 530, 0), (5, 80, 0), (5, 80, 0), (6, 80, 0), (6, 80, 0), (12, 230, 150), (13, 60, 0), (14, 60, 0), (15, 3590, 0), (16, 60, 0), (16, 60, 0), (17, 60, 0), (17, 60, 0), (19, 60, 0), (20, 60, 0), (24, 650, 120), (25, 60, 0), (25, 60, 0), (29, 60, 0), (29, 60, 0), (32, 1250, 800), (35, 100, 0)], [(1, 100, 0), (2, 650, 200), (3, 95, 0), (5, 65, 0), (6, 60, 0), (12, 230, 150), (13, 60, 0), (8, 60, 0), (9, 170, 0), (9, 170, 0), (8, 60, 0), (10, 55, 0), (11, 55, 0), (12, 230, 150), (13, 60, 0), (15, 3950, 0), (16, 70, 0), (17, 55, 0), (17, 60, 0), (19, 60, 0), (19, 60, 0), (20, 60, 0), (20, 70, 0), (27, 1250, 180), (28, 60, 0), (28, 70, 0), (25, 60, 0), (26, 590, 120), (25, 60, 0), (29, 60, 0), (31, 830, 800), (37, 1250, 800), (35, 100, 0)], [(1, 100, 0), (7, 350, 0), (10, 60, 0), (11, 60, 0), (12, 140, 150), (13, 55, 0), (14, 55, 0), (15, 1850, 0), (16, 70, 0), (16, 70, 0), (17, 70, 0), (17, 70, 0), (19, 60, 0), (20, 70, 0), (24, 590, 120), (25, 70, 0), (29, 70, 0), (32, 650, 800), (37, 950, 800), (35, 100, 0)], [(1, 100, 0), (2, 350, 200), (4, 950, 0), (5, 65, 0), (6, 65, 0), (12, 110, 150), (13, 60, 0), (14, 60, 0), (15, 2450, 0), (16, 60, 0), (17, 60, 0), (19, 60, 0), (20, 60, 0), (24, 650, 120), (25, 60, 0), (29, 60, 0), (32, 1250, 800), (35, 100, 0)], [(1, 100, 0), (7, 350, 0), (10, 60, 0), (11, 60, 0), (12, 230, 150), (13, 60, 0), (8, 60, 0), (9, 170, 0), (9, 170, 0), (8, 60, 0), (10, 55, 0), (11, 55, 0), (12, 230, 150), (13, 60, 0), (14, 60, 0), (15, 3950, 0), (16, 70, 0), (17, 60, 0), (17, 60, 0), (19, 60, 0), (19, 60, 0), (20, 60, 0), (20, 70, 0), (27, 1250, 180), (28, 60, 0), (25, 60, 0), (26, 470, 120), (25, 60, 0), (29, 60, 0), (31, 830, 800), (37, 950, 800), (35, 100, 0)]]
    machines_count = 1 + max(task[0] for job in jobs_data for task in job)
    all_machines = range(machines_count)
    # Computes horizon dynamically as the sum of all durations.
    horizon = sum(task[1] for job in jobs_data for task in job)

    # Create the model.
    model = cp_model.CpModel()

    # Named tuple to store information about created variables.
    task_type = collections.namedtuple("task_type", "start end interval intervalDrift endDrift machine duration")
    # Named tuple to manipulate solution information.
    assigned_task_type = collections.namedtuple(
        "assigned_task_type", "start job index duration machine"
    )

    # Creates job intervals and add to the corresponding machine lists.
    all_tasks = {}
    machine_to_intervals = collections.defaultdict(list)

    for job_id, job in enumerate(jobs_data):
        for task_id, task in enumerate(job):
            
            machine, duration,derive = task
            #duration-=50
            derive=0
            tps_dep_moy=0
            suffix = f"_{job_id}_{task_id}"
            start_var = model.new_int_var(0, horizon, "start" + suffix)
            end_var = model.new_int_var(0, horizon, "end" + suffix)
            end_var_drift = model.new_int_var(0, horizon, "end drift" + suffix)
            interval_var = model.new_interval_var(start_var, duration+tps_dep_moy, end_var, "interval" + suffix)
            intervalDrift=model.new_interval_var(start_var, duration+derive+tps_dep_moy, end_var_drift, "interval drift" + suffix)
            all_tasks[job_id, task_id] = task_type(
                start=start_var, end=end_var, interval=interval_var,
                intervalDrift=intervalDrift,endDrift=end_var_drift,
                machine=machine,
                duration=duration
            )
            machine_to_intervals[machine].append(intervalDrift)

   
    
    bridgesMoves(model,jobs_data,machine_to_intervals,all_tasks,horizon)

    set_no_overlap(all_machines,machine_to_intervals,model)

    for job_id, job in enumerate(jobs_data):
        for task_id in range(len(job) - 1):
            task=all_tasks[job_id, task_id]
            model.add(all_tasks[job_id, task_id + 1].start >= task.end)  
            model.add(all_tasks[job_id, task_id + 1].start <= task.endDrift)            
            #model.add(all_tasks[job_id, task_id + 1].start <= all_tasks[job_id, task_id].end_drift)
            #model.add(all_tasks[job_id, task_id + 1].start >= all_tasks[job_id, task_id].end+40)

    # Creates the solver and solve.
    solver = cp_model.CpSolver()
    # Makespan objective.
    obj_var = model.new_int_var(0, horizon, "makespan")
    model.add_max_equality(
        obj_var,
        [all_tasks[job_id, len(job) - 1].endDrift for job_id, job in enumerate(jobs_data)],
    )
    model.minimize(obj_var)
  

    
    solver.parameters.max_time_in_seconds = consts.MAX_TIME
    status = solver.solve(model)


    if status == cp_model.OPTIMAL or status == cp_model.FEASIBLE:
        if status == cp_model.OPTIMAL : print("OPTIMAL !!")
        print("Solution:")
        # Create one list of assigned tasks per machine.
        assigned_jobs = collections.defaultdict(list)
        for job_id, job in enumerate(jobs_data):
            for task_id, task in enumerate(job):
                machine = task[0]
                assigned_jobs[machine].append(
                    assigned_task_type(
                        start=solver.value(all_tasks[job_id, task_id].start),
                        job=job_id,
                        index=task_id,
                        duration=solver.value(all_tasks[job_id, task_id].endDrift)-
                            solver.value(all_tasks[job_id, task_id].start),
                        machine=machine
                    )
                )

        # Create per machine output lines.
        output = ""
        for machine in all_machines:
            # Sort by starting time.
            assigned_jobs[machine].sort()
            sol_line_tasks = "Machine " + str(machine) + ": "
            sol_line = "           "

            for assigned_task in assigned_jobs[machine]:
                name = f"job_{assigned_task.job}_task_{assigned_task.index}"
                # add spaces to output to align columns.
                sol_line_tasks += f"{name:15}"

                start = assigned_task.start
                duration = assigned_task.duration
                sol_tmp = f"[{start},{start + duration}]"
                # add spaces to output to align columns.
                sol_line += f"{sol_tmp:15}"

            sol_line += "\n"
            sol_line_tasks += "\n"
            output += sol_line_tasks
            output += sol_line

        gantt (assigned_jobs)
        # Finally print the solution found.
        
        print(output)
        print(f"Optimal Schedule Length: {solver.objective_value}")
        
       

        # Statistics.
        print("\nStatistics")
        print(f"  - conflicts: {solver.num_conflicts}")
        print(f"  - branches : {solver.num_branches}")
        print(f"  - wall time: {solver.wall_time}s")
    else :
        print(f"NO SOLUTION")

def set_no_overlap(all_machines,machine_to_intervals,model) ->None:
    cumulMachines33=[]
    # Create and add disjunctive constraints.
    for machine in all_machines:

        match machine:
            case consts.MIDDLE_MACHINE:
                capacity = 3  # Maximum capacity of 2 machines in use at any time 
                model.add_cumulative(
                    machine_to_intervals[machine],
                    [1] * len(machine_to_intervals[machine]),  # Demand of 1 for every interval
                    capacity)
          
           
            case 33|32|31:
                cumulMachines33.append(machine_to_intervals[machine])
                if( machine==32 or machine==31):
                    model.add_no_overlap(machine_to_intervals[machine])   
            case _:
                model.add_no_overlap(machine_to_intervals[machine])    
    
    
    #for machine 33 ...
    liste_aplatie = [item for sous_liste in cumulMachines33 for item in sous_liste]
    model.add_cumulative(liste_aplatie,[1] * len(liste_aplatie),  2)

def  bridgesMoves (model,jobs_data,machine_to_intervals,all_tasks,horizon) ->None:
    #simulate bridge moves per area
    brigesMoves= collections.defaultdict(list)
    # when bridge 1 put down on machine 15
    # bridge 2 must be far away
    zonesAnoP1P2=[]
    securityZonesP1P2=[]
    for job_id, job in enumerate(jobs_data):
        for task_id in range(len(job) - 1):
            
            task=all_tasks[job_id, task_id ]
            bridge=0
            suffix = f"_{job_id}_{task_id}"
            if(task.machine >=15) : bridge=1

            if(task.machine in [12,13,14,16,17] ) :
                securityZonesP1P2.append(task.intervalDrift)

            if(task.duration < consts.ALLOW_BRIGE_MOVE_TIME):               
                brigesMoves[bridge].append(task.intervalDrift)
            else:               
                start_var_fin = model.new_int_var(0, horizon, "start_var_fin" + suffix)
                end_var_deb=model.new_int_var(0, horizon, "end_var_deb" + suffix)
                interval_var_deb = model.new_interval_var(
                    task.start, consts.BRIDGE_MOVE_TIME, end_var_deb, "interval end_var_deb" + suffix
                )
                interval_var_end = model.new_interval_var(
                    start_var_fin, consts.BRIDGE_MOVE_TIME, task.end, "interval end_var_deb" + suffix
                )
               
                if(task.machine ==consts.MIDDLE_MACHINE) :                    
                  
                    interval_surround_P1 = model.new_interval_var(
                         task.start-consts.BRIDGE_MOVE_TIME, consts.BRIDGE_MOVE_TIME*2,model.new_int_var(0, horizon, "end_var_deb" + suffix), "surround P1 interval end_var_deb" + suffix
                    )
                  
                    interval_surround_P2 = model.new_interval_var(
                        task.end-consts.BRIDGE_MOVE_TIME, consts.BRIDGE_MOVE_TIME*2,model.new_int_var(0, horizon, "end_var_end" + suffix) , "surround P2 interval end_var_end" + suffix
                    )
                    zonesAnoP1P2.append(interval_surround_P1)
                    zonesAnoP1P2.append(interval_surround_P2)
                    brigesMoves[0].append(interval_var_deb)
                    brigesMoves[1].append(interval_var_end)
                    securityZonesP1P2.append(interval_var_deb)
                    securityZonesP1P2.append(interval_var_end)
                else:
                    brigesMoves[bridge].append(interval_var_deb)
                    brigesMoves[bridge].append(interval_var_end)
                
                    

    for brigeMoves in brigesMoves:
        model.add_no_overlap(brigesMoves[brigeMoves])
    
    #model.add_no_overlap(zonesAnoP1P2)
    model.add_no_overlap(securityZonesP1P2)

class ZoneCumul:
    def __init__(self, cumul):
        """
        Initialise une instance de ZoneCumul.

        :param cumul: Nombre de postes dans la zone de cumul.
        """
        self.cumul = cumul
        self.lastTimeAtPostes = [0] * cumul  # Tableau initialisé à 0.

    def getPosteIdx(self, starttime, endtime):
       
        zonePrise = False
        idxPoste = 0

        # Parcourt les postes à l'envers
        for i in range(len(self.lastTimeAtPostes) - 1, -1, -1):
            if self.lastTimeAtPostes[i] == 0 and not zonePrise:
                # Si le poste est disponible (non utilisé)
                self.lastTimeAtPostes[i] = endtime
                zonePrise = True
                return i
            elif self.lastTimeAtPostes[i] <= starttime:
                # Si le poste est occupé mais que son temps est dépassé
                self.lastTimeAtPostes[i] = 0
                if not zonePrise:
                    self.lastTimeAtPostes[i] = endtime
                    zonePrise = True
                    idxPoste = i

        return idxPoste  # Retourne l'index trouvé ou le premier disponible

def  gantt (assigned_jobs) ->None:
    
    # Séparer les tâches par machine
    # Séparer les tâches par machine
   
    zonesCumul={}
    zonesCumul[15]=ZoneCumul(cumul=3)
    zonesCumul[33]=ZoneCumul(cumul=2)
    zonesCumul[1]=ZoneCumul(cumul=2)
    zonesCumul[35]=ZoneCumul(cumul=2)

    # Création de la figure
    fig, ax = plt.subplots(figsize=(12, 8))

    # Couleurs pour différencier les jobs
    colors = plt.cm.tab20
    job_colors = {}

    # Gérer les tâches de la machine 15
    machine_15_tasks = assigned_jobs[15]


    # Traiter chaque machine
    for machine, machine_tasks in assigned_jobs.items():
        if machine in [15,1,35,33]:
            # Répartir les tâches de la machine 15 sur 15.1, 15.2 et 15.3
            for task in machine_tasks:
            
                cumulZone=zonesCumul[machine]
                idx =    cumulZone.getPosteIdx(task.start,task.start+ task.duration)-1
                ax.broken_barh(
                    [(task.start, task.duration)],
                    (machine - ((idx * 0.2) ), 0.2),
                    facecolors=job_colors.setdefault(
                        task.job, colors(len(job_colors) % 20)
                    ),
                    edgecolor="black",
                    label=f"Job {task.job}" if task.job not in job_colors else None,
                )
                
            
        else:
            # Traiter les tâches des autres machines
            for task in machine_tasks:
                ax.broken_barh(
                    [(task.start, task.duration)],
                    (machine - 0.4, 0.8),
                    facecolors=job_colors.setdefault(
                        task.job, colors(len(job_colors) % 20)
                    ),
                    edgecolor="black",
                    label=f"Job {task.job}" if task.job not in job_colors else None,
                )

    # Ajouter les étiquettes
    ax.set_xlabel("Temps")
    ax.set_ylabel("Machines")
    ax.set_title("Diagramme de Gantt")
    ax.set_yticks(list(assigned_jobs.keys()) )
    ax.set_yticklabels([str(machine) for machine in assigned_jobs.keys()] )
    #ax.legend(loc="upper right")

    # Afficher la figure
    plt.tight_layout()
    plt.show()




if __name__ == "__main__":
    main()

