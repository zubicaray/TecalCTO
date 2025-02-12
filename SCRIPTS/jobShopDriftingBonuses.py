"""Minimal jobshop example."""
import collections
from ortools.sat.python import cp_model
import matplotlib.pyplot as plt
import matplotlib.dates as mdates
from collections import defaultdict
import types

consts = types.SimpleNamespace()
consts.MAX_TIME=120


PENALTIES= False
SECURITY_P1_P2=True
# if the task duration is above ALLOW_EXTRA_JOB_MOVE_TIME
# the bridge can go else where
consts.ALLOW_EXTRA_JOB_MOVE_TIME=180
consts.BRIDGE_MOVE_TIME=30

#the frontiere between the two bridges areas
consts.MIDDLE_MACHINE=15
consts.C33_MACHINE=33
consts.C0_MACHINE=1
consts.C35_MACHINE=35

consts.CUMULS_MACHINE=[consts.MIDDLE_MACHINE,consts.C33_MACHINE,consts.C0_MACHINE,consts.C35_MACHINE]


NOOVERLAP_JOBS={}
long_task_count={}
penalties=[]
 # Named tuple to store information about created variables.
task_type = collections.namedtuple("task_type", "start end interval intervalDrift endDrift machine duration derive tpsDep")
# Named tuple to manipulate solution information.
assigned_task_type = collections.namedtuple(
    "assigned_task_type", "start job index duration machine"
)
def main() -> None:
    """Minimal jobshop problem."""
    # Data.
    jobs_data_big = [[(1, 100, 54, 0), (7, 240, 56, 0), (11, 30, 34, 0), (11, 30, 48, 0), (12, 120, 49, 150), (13, 10, 48, 0), (14, 10, 46, 0), (15, 4500, 54, 0), (16, 10, 48, 0), (17, 10, 47, 0), (19, 10, 34, 0), (19, 20, 48, 0), (20, 10, 32, 0), (20, 30, 51, 0), (24, 300, 47, 120), (25, 10, 50, 0), (29, 10, 34, 0), (29, 30, 51, 0), (32, 600, 58, 800), (37, 1200, 43, 800), (35, 100, 43, 0)], [(1, 100, 43, 0), (2, 300, 46, 200), (3, 180, 47, 0), (4, 300, 53, 0), (5, 10, 47, 0), (6, 10, 55, 0), (12, 135, 49, 150), (13, 10, 48, 0), (14, 10, 46, 0), (15, 4320, 54, 0), (16, 10, 48, 0), (17, 10, 47, 0), (19, 10, 48, 0), (20, 10, 56, 0), (27, 941, 47, 180), (28, 20, 51, 0), (25, 10, 48, 0), (26, 480, 46, 120), (25, 10, 50, 0), (29, 10, 53, 0), (31, 900, 53, 800), (37, 900, 43, 800), (35, 100, 43, 0)], [(1, 100, 43, 0), (2, 920, 47, 200), (4, 480, 58, 0), (5, 30, 33, 0), (5, 30, 47, 0), (6, 30, 34, 0), (6, 30, 55, 0), (12, 180, 49, 150), (13, 10, 48, 0), (14, 10, 46, 0), (15, 3360, 54, 0), (16, 10, 35, 0), (16, 10, 48, 0), (17, 10, 34, 0), (17, 10, 47, 0), (19, 10, 48, 0), (20, 10, 51, 0), (24, 600, 47, 120), (25, 10, 34, 0), (25, 10, 50, 0), (29, 10, 34, 0), (29, 10, 46, 0), (32, 1200, 56, 800), (35, 100, 56, 0)], [(1, 100, 43, 0), (2, 300, 47, 200), (4, 180, 53, 0), (5, 10, 47, 0), (6, 10, 70, 0), (12, 180, 49, 150), (13, 10, 54, 0), (8, 10, 50, 0), (9, 240, 53, 0), (8, 30, 49, 0), (10, 5, 49, 0), (12, 180, 49, 150), (13, 10, 47, 0), (15, 2520, 54, 0), (16, 10, 48, 0), (17, 10, 47, 0), (19, 10, 48, 0), (20, 10, 56, 0), (27, 900, 47, 180), (28, 40, 51, 0), (25, 10, 43, 0), (26, 540, 46, 120), (25, 10, 50, 0), (29, 10, 53, 0), (31, 1200, 55, 800), (35, 100, 55, 0)], [(1, 100, 54, 0), (7, 240, 56, 0), (11, 30, 34, 0), (11, 30, 48, 0), (12, 120, 49, 150), (13, 10, 48, 0), (14, 10, 46, 0), (15, 4500, 54, 0), (16, 10, 48, 0), (17, 10, 47, 0), (19, 10, 34, 0), (19, 20, 48, 0), (20, 10, 32, 0), (20, 30, 51, 0), (24, 300, 47, 120), (25, 10, 50, 0), (29, 10, 34, 0), (29, 30, 51, 0), (32, 600, 58, 800), (37, 1200, 43, 800), (35, 100, 43, 0)], [(1, 100, 43, 0), (2, 720, 47, 200), (4, 480, 58, 0), (5, 30, 33, 0), (5, 30, 47, 0), (6, 30, 34, 0), (6, 30, 55, 0), (12, 180, 49, 150), (13, 10, 48, 0), (14, 10, 46, 0), (15, 3540, 54, 0), (16, 10, 35, 0), (16, 10, 48, 0), (17, 10, 34, 0), (17, 10, 47, 0), (19, 10, 48, 0), (20, 10, 51, 0), (24, 600, 47, 120), (25, 10, 34, 0), (25, 10, 50, 0), (29, 10, 34, 0), (29, 10, 46, 0), (32, 1200, 56, 800), (35, 100, 56, 0)], [(1, 100, 43, 0), (2, 600, 46, 200), (3, 45, 51, 0), (5, 15, 47, 0), (6, 10, 55, 0), (12, 180, 49, 150), (13, 10, 54, 0), (8, 10, 50, 0), (9, 120, 36, 0), (9, 120, 43, 0), (8, 10, 49, 0), (10, 5, 49, 0), (11, 5, 48, 0), (12, 180, 49, 150), (13, 10, 52, 0), (15, 3900, 49, 0), (16, 20, 48, 0), (17, 5, 34, 0), (17, 10, 47, 0), (19, 10, 34, 0), (19, 10, 48, 0), (20, 10, 32, 0), (20, 20, 56, 0), (27, 1200, 47, 180), (28, 10, 33, 0), (28, 20, 51, 0), (25, 10, 48, 0), (26, 540, 46, 120), (25, 10, 50, 0), (29, 10, 53, 0), (31, 780, 58, 800), (37, 1200, 43, 800), (35, 100, 43, 0)], [(1, 100, 54, 0), (7, 300, 50, 0), (10, 10, 49, 0), (11, 10, 48, 0), (12, 90, 44, 150), (13, 5, 48, 0), (14, 5, 46, 0), (15, 1800, 54, 0), (16, 20, 40, 0), (16, 20, 48, 0), (17, 20, 39, 0), (17, 20, 47, 0), (19, 10, 48, 0), (20, 20, 51, 0), (24, 540, 47, 120), (25, 20, 50, 0), (29, 20, 46, 0), (32, 600, 53, 800), (37, 900, 43, 800), (35, 100, 43, 0)], [(1, 100, 43, 0), (2, 300, 47, 200), (4, 900, 48, 0), (5, 15, 47, 0), (6, 15, 55, 0), (12, 60, 49, 150), (13, 10, 48, 0), (14, 10, 46, 0), (15, 2400, 54, 0), (16, 10, 48, 0), (17, 10, 47, 0), (19, 10, 48, 0), (20, 10, 51, 0), (24, 600, 47, 120), (25, 10, 50, 0), (29, 10, 46, 0), (32, 1200, 56, 800), (35, 100, 56, 0)], [(1, 100, 54, 0), (7, 300, 50, 0), (10, 10, 49, 0), (11, 10, 48, 0), (12, 180, 49, 150), (13, 10, 54, 0), (8, 10, 50, 0), (9, 120, 36, 0), (9, 120, 43, 0), (8, 10, 49, 0), (10, 5, 49, 0), (11, 5, 48, 0), (12, 180, 49, 150), (13, 10, 48, 0), (14, 10, 46, 0), (15, 3900, 59, 0), (16, 20, 48, 0), (17, 10, 34, 0), (17, 10, 47, 0), (19, 10, 34, 0), (19, 10, 48, 0), (20, 10, 32, 0), (20, 20, 56, 0), (27, 1200, 47, 180), (28, 10, 51, 0), (25, 10, 48, 0), (26, 420, 46, 120), (25, 10, 50, 0), (29, 10, 53, 0), (31, 780, 58, 800), (37, 900, 43, 800), (35, 100, 43, 0)], [(1, 100, 54, 0), (7, 300, 50, 0), (10, 10, 49, 0), (8, 10, 50, 0), (9, 120, 43, 0), (8, 30, 49, 0), (10, 10, 49, 0), (11, 10, 48, 0), (12, 60, 49, 150), (13, 10, 48, 0), (14, 10, 46, 0), (15, 2880, 54, 0), (16, 10, 48, 0), (17, 10, 47, 0), (19, 10, 48, 0), (21, 60, 104, 0), (19, 10, 48, 0), (20, 10, 51, 0), (24, 600, 47, 120), (25, 10, 50, 0), (29, 10, 46, 0), (32, 900, 56, 800), (35, 100, 56, 0)], [(1, 100, 54, 0), (7, 240, 56, 0), (11, 30, 34, 0), (11, 30, 48, 0), (12, 120, 49, 150), (13, 10, 48, 0), (14, 10, 46, 0), (15, 4500, 54, 0), (16, 10, 48, 0), (17, 10, 47, 0), (19, 10, 34, 0), (19, 20, 48, 0), (20, 10, 32, 0), (20, 30, 51, 0), (24, 300, 47, 120), (25, 10, 50, 0), (29, 10, 34, 0), (29, 30, 51, 0), (32, 600, 58, 800), (37, 1200, 43, 800), (35, 100, 43, 0)], [(1, 100, 43, 0), (2, 300, 52, 200)], [(1, 100, 0, 0), (3, 120, 46, 0), (5, 10, 52, 0)], [(1, 100, 43, 0), (2, 300, 47, 200), (4, 180, 53, 0), (5, 10, 47, 0), (6, 10, 70, 0), (12, 180, 49, 150), (13, 10, 54, 0), (8, 10, 50, 0), (9, 240, 53, 0), (8, 30, 49, 0), (10, 5, 49, 0), (12, 180, 49, 150), (13, 10, 47, 0), (15, 2400, 54, 0), (16, 10, 48, 0), (17, 10, 47, 0), (19, 10, 48, 0), (20, 10, 56, 0), (27, 900, 47, 180), (28, 40, 51, 0), (25, 10, 43, 0), (26, 540, 46, 120), (25, 10, 50, 0), (29, 10, 53, 0), (31, 1200, 55, 800), (35, 100, 55, 0)], [(1, 100, 54, 0), (7, 240, 56, 0), (11, 30, 34, 0), (11, 30, 48, 0), (12, 120, 49, 150), (13, 10, 48, 0), (14, 10, 46, 0), (15, 4500, 54, 0), (16, 10, 48, 0), (17, 10, 47, 0), (19, 10, 34, 0), (19, 20, 48, 0), (20, 10, 32, 0), (20, 30, 51, 0), (24, 300, 47, 120), (25, 10, 50, 0), (29, 10, 34, 0), (29, 30, 51, 0), (32, 600, 58, 800), (37, 1200, 43, 800), (35, 100, 43, 0)], [(1, 100, 43, 0), (2, 600, 46, 200), (3, 45, 51, 0), (5, 15, 47, 0), (6, 10, 55, 0), (12, 180, 49, 150), (13, 10, 54, 0), (8, 10, 50, 0), (9, 120, 36, 0), (9, 120, 43, 0), (8, 10, 49, 0), (10, 5, 49, 0), (11, 5, 48, 0), (12, 180, 49, 150), (13, 10, 52, 0), (15, 3900, 49, 0), (16, 20, 48, 0), (17, 5, 34, 0), (17, 10, 47, 0), (19, 10, 34, 0), (19, 10, 48, 0), (20, 10, 32, 0), (20, 20, 56, 0), (27, 1200, 47, 180), (28, 10, 33, 0), (28, 20, 51, 0), (25, 10, 48, 0), (26, 480, 46, 120), (25, 10, 50, 0), (29, 10, 53, 0), (31, 780, 58, 800), (37, 900, 43, 800), (35, 100, 43, 0)], [(1, 100, 43, 0), (2, 720, 47, 200), (4, 480, 58, 0), (5, 30, 33, 0), (5, 30, 47, 0), (6, 30, 34, 0), (6, 30, 55, 0), (12, 180, 49, 150), (13, 10, 48, 0), (14, 10, 46, 0), (15, 3360, 54, 0), (16, 10, 35, 0), (16, 10, 48, 0), (17, 10, 34, 0), (17, 10, 47, 0), (19, 10, 48, 0), (20, 10, 51, 0), (24, 600, 47, 120), (25, 10, 34, 0), (25, 10, 50, 0), (29, 10, 34, 0), (29, 10, 46, 0), (32, 1200, 56, 800), (35, 100, 56, 0)], [(1, 100, 54, 0), (7, 300, 50, 0), (10, 10, 49, 0), (11, 10, 48, 0), (12, 180, 49, 150), (13, 10, 54, 0), (8, 10, 50, 0), (9, 120, 36, 0), (9, 120, 43, 0), (8, 10, 49, 0), (10, 5, 49, 0), (11, 5, 48, 0), (12, 180, 49, 150), (13, 10, 48, 0), (14, 10, 46, 0), (15, 3900, 59, 0), (16, 20, 48, 0), (17, 10, 34, 0), (17, 10, 47, 0), (19, 10, 34, 0), (19, 10, 48, 0), (20, 10, 32, 0), (20, 20, 56, 0), (27, 1200, 47, 180), (28, 10, 51, 0), (25, 10, 48, 0), (26, 480, 46, 120), (25, 10, 50, 0), (29, 10, 53, 0), (31, 780, 58, 800), (37, 1080, 43, 800), (35, 100, 43, 0)], [(1, 100, 43, 0), (2, 300, 47, 200), (4, 600, 53, 0), (5, 30, 33, 0), (5, 30, 47, 0), (6, 30, 34, 0), (6, 30, 55, 0), (12, 120, 49, 150), (13, 30, 48, 0), (14, 30, 46, 0), (15, 4200, 59, 0), (16, 30, 48, 0), (17, 30, 47, 0), (19, 30, 34, 0), (19, 30, 48, 0), (20, 20, 32, 0), (20, 20, 56, 0), (27, 1200, 52, 180), (28, 30, 51, 0), (25, 10, 48, 0), (26, 600, 51, 120), (25, 10, 50, 0), (29, 10, 58, 0), (31, 900, 58, 800), (37, 600, 43, 800), (35, 100, 43, 0)], [(1, 100, 43, 0), (2, 300, 47, 200), (4, 180, 53, 0), (5, 10, 47, 0), (6, 10, 70, 0), (12, 180, 49, 150), (13, 10, 54, 0), (8, 10, 50, 0), (9, 240, 53, 0), (8, 30, 49, 0), (10, 5, 49, 0), (12, 180, 49, 150), (13, 10, 47, 0), (15, 2400, 54, 0), (16, 10, 48, 0), (17, 10, 47, 0), (19, 10, 48, 0), (20, 10, 56, 0), (27, 900, 47, 180), (28, 40, 51, 0), (25, 10, 43, 0), (26, 420, 46, 120), (25, 10, 50, 0), (29, 10, 53, 0), (31, 1200, 55, 800), (35, 100, 55, 0)], [(1, 100, 54, 0), (7, 240, 56, 0), (11, 30, 34, 0), (11, 30, 48, 0), (12, 120, 49, 150), (13, 10, 48, 0), (14, 10, 46, 0), (15, 4500, 54, 0), (16, 10, 48, 0), (17, 10, 47, 0), (19, 10, 34, 0), (19, 20, 48, 0), (20, 10, 32, 0), (20, 30, 51, 0), (24, 300, 47, 120), (25, 10, 50, 0), (29, 10, 34, 0), (29, 30, 51, 0), (32, 600, 58, 800), (37, 900, 43, 800), (35, 100, 43, 0)], [(1, 100, 54, 0), (7, 300, 50, 0), (10, 5, 49, 0), (11, 10, 48, 0), (12, 180, 49, 150), (13, 10, 52, 0), (15, 3840, 54, 0), (16, 10, 48, 0), (17, 10, 60, 0), (12, 240, 49, 150), (13, 10, 48, 0), (14, 10, 61, 0), (19, 10, 48, 0), (20, 10, 32, 0), (20, 10, 49, 0), (22, 120, 34, 0), (22, 75, 64, 0), (20, 10, 32, 0), (20, 10, 61, 0), (29, 300, 58, 0), (33, 3900, 56, 800), (37, 900, 43, 800), (35, 100, 43, 0)], [(1, 100, 43, 0), (2, 300, 47, 200), (4, 900, 48, 0), (5, 15, 47, 0), (6, 15, 55, 0), (12, 60, 49, 150), (13, 10, 48, 0), (14, 10, 46, 0), (15, 2520, 54, 0), (16, 10, 48, 0), (17, 10, 47, 0), (19, 10, 48, 0), (20, 10, 51, 0), (24, 600, 47, 120), (25, 10, 50, 0), (29, 10, 46, 0), (32, 1230, 56, 800), (35, 100, 56, 0)], [(1, 100, 43, 0), (2, 300, 47, 200), (4, 180, 53, 0), (5, 10, 47, 0), (6, 10, 70, 0), (12, 180, 49, 150), (13, 10, 54, 0), (8, 10, 50, 0), (9, 240, 53, 0), (8, 30, 49, 0), (10, 5, 49, 0), (12, 210, 49, 150), (13, 10, 47, 0), (15, 2430, 54, 0), (16, 10, 48, 0), (17, 10, 47, 0), (19, 10, 48, 0), (20, 10, 56, 0), (27, 900, 47, 180), (28, 40, 51, 0), (25, 10, 43, 0), (26, 540, 46, 120), (25, 10, 50, 0), (29, 10, 53, 0), (31, 1200, 55, 800), (35, 100, 55, 0)], [(1, 100, 54, 0), (7, 240, 56, 0), (11, 30, 34, 0), (11, 30, 48, 0), (12, 120, 49, 150), (13, 10, 48, 0), (14, 10, 46, 0), (15, 4500, 54, 0), (16, 10, 48, 0), (17, 10, 47, 0), (19, 10, 34, 0), (19, 20, 48, 0), (20, 10, 32, 0), (20, 30, 51, 0), (24, 300, 47, 120), (25, 10, 50, 0), (29, 10, 34, 0), (29, 30, 51, 0), (32, 600, 58, 800), (37, 960, 43, 800), (35, 100, 43, 0)], [(1, 100, 43, 0), (2, 300, 47, 200), (4, 300, 53, 0), (5, 10, 47, 0), (6, 10, 70, 0), (12, 180, 49, 150), (13, 10, 54, 0), (8, 10, 50, 0), (9, 240, 53, 0), (8, 30, 49, 0), (10, 5, 49, 0), (12, 180, 49, 150), (13, 10, 47, 0), (15, 2700, 54, 0), (16, 10, 48, 0), (17, 10, 47, 0), (19, 10, 48, 0), (20, 10, 56, 0), (27, 900, 47, 180), (28, 40, 51, 0), (25, 10, 43, 0), (26, 540, 46, 120), (25, 10, 50, 0), (29, 10, 53, 0), (31, 1500, 58, 800), (37, 900, 43, 800), (35, 100, 43, 0)], [(1, 100, 54, 0), (7, 270, 56, 0), (11, 30, 34, 0), (11, 30, 48, 0), (12, 120, 49, 150), (13, 10, 48, 0), (14, 10, 46, 0), (15, 4500, 54, 0), (16, 10, 48, 0), (17, 10, 47, 0), (19, 10, 34, 0), (19, 20, 48, 0), (20, 10, 32, 0), (20, 30, 51, 0), (24, 300, 47, 120), (25, 10, 50, 0), (29, 10, 34, 0), (29, 30, 51, 0), (32, 600, 58, 800), (37, 1200, 43, 800), (35, 100, 43, 0)], [(1, 100, 43, 0), (2, 720, 47, 200), (4, 480, 58, 0), (5, 30, 33, 0), (5, 30, 47, 0), (6, 30, 34, 0), (6, 30, 55, 0), (12, 180, 49, 150), (13, 10, 48, 0), (14, 10, 46, 0), (15, 3360, 54, 0), (16, 10, 35, 0), (16, 10, 48, 0), (17, 10, 34, 0), (17, 10, 47, 0), (19, 10, 48, 0), (20, 10, 51, 0), (24, 600, 47, 120), (25, 10, 34, 0), (25, 10, 50, 0), (29, 10, 34, 0), (29, 10, 46, 0), (32, 1200, 56, 800), (35, 100, 56, 0)], [(1, 100, 43, 0), (2, 720, 47, 200), (4, 480, 58, 0), (5, 30, 33, 0), (5, 30, 47, 0), (6, 30, 34, 0), (6, 30, 55, 0), (12, 180, 49, 150), (13, 10, 48, 0), (14, 10, 46, 0), (15, 4035, 54, 0), (16, 10, 35, 0), (16, 10, 48, 0), (17, 10, 34, 0), (17, 10, 47, 0), (19, 10, 48, 0), (20, 10, 51, 0), (24, 600, 47, 120), (25, 10, 34, 0), (25, 10, 50, 0), (29, 10, 34, 0), (29, 10, 46, 0), (32, 1200, 56, 800), (35, 100, 56, 0)], [(1, 100, 54, 0), (7, 420, 50, 0), (10, 10, 47, 0), (9, 60, 43, 0), (8, 10, 49, 0), (10, 5, 49, 0), (11, 5, 48, 0), (12, 180, 49, 150), (13, 10, 48, 0), (14, 10, 46, 0), (15, 4500, 59, 0), (16, 20, 48, 0), (17, 10, 34, 0), (17, 10, 47, 0), (19, 10, 34, 0), (19, 10, 48, 0), (20, 10, 32, 0), (20, 20, 56, 0), (27, 30, 33, 180), (27, 1200, 47, 180), (28, 10, 51, 0), (25, 10, 48, 0), (26, 360, 46, 120), (25, 10, 50, 0), (29, 10, 53, 0), (31, 780, 58, 800), (37, 900, 43, 800), (35, 100, 43, 0)], [(1, 100, 43, 0), (2, 600, 46, 200), (3, 45, 51, 0), (5, 15, 47, 0), (6, 10, 55, 0), (12, 180, 49, 150), (13, 10, 54, 0), (8, 10, 50, 0), (9, 120, 36, 0), (9, 120, 43, 0), (8, 10, 49, 0), (10, 5, 49, 0), (11, 5, 48, 0), (12, 180, 49, 150), (13, 10, 52, 0), (15, 3900, 49, 0), (16, 20, 48, 0), (17, 5, 34, 0), (17, 10, 47, 0), (19, 10, 34, 0), (19, 10, 48, 0), (20, 10, 32, 0), (20, 20, 56, 0), (27, 1200, 47, 180), (28, 10, 33, 0), (28, 20, 51, 0), (25, 10, 48, 0), (26, 540, 46, 120), (25, 10, 50, 0), (29, 10, 53, 0), (31, 780, 58, 800), (37, 600, 43, 800), (35, 100, 43, 0)], [(1, 100, 43, 0), (2, 300, 72, 200), (4, 960, 73, 0), (5, 15, 33, 0), (5, 30, 72, 0), (6, 15, 34, 0), (6, 30, 80, 0), (12, 180, 74, 150), (13, 5, 53, 0), (14, 5, 51, 0), (15, 4200, 69, 0), (16, 10, 63, 0), (17, 10, 62, 0), (19, 10, 53, 0), (20, 10, 66, 0), (24, 600, 62, 120), (25, 10, 65, 0), (29, 10, 61, 0), (32, 1200, 73, 800), (37, 1200, 43, 800), (35, 100, 43, 0)], [(1, 100, 54, 0), (7, 300, 51, 0), (11, 10, 51, 0), (8, 10, 50, 0), (9, 120, 53, 0), (8, 10, 49, 0), (10, 10, 49, 0), (11, 10, 48, 0), (12, 60, 49, 150), (13, 5, 48, 0), (14, 5, 46, 0), (15, 1500, 54, 0), (16, 10, 35, 0), (16, 10, 48, 0), (17, 10, 34, 0), (17, 10, 47, 0), (19, 10, 48, 0), (20, 10, 46, 0), (24, 480, 47, 120), (25, 10, 50, 0), (29, 10, 46, 0), (32, 600, 56, 800), (35, 100, 56, 0)], [(1, 100, 43, 0), (2, 300, 47, 200), (4, 300, 48, 0), (5, 30, 47, 0), (6, 30, 70, 0), (12, 60, 49, 150), (13, 5, 48, 0), (14, 5, 46, 0), (15, 1500, 54, 0), (16, 10, 48, 0), (17, 10, 47, 0), (19, 10, 48, 0), (20, 10, 51, 0), (24, 600, 47, 120), (25, 10, 50, 0), (29, 10, 46, 0), (32, 600, 56, 800), (35, 100, 56, 0)], [(1, 100, 54, 0), (7, 240, 56, 0), (11, 30, 34, 0), (11, 30, 48, 0), (12, 120, 49, 150), (13, 10, 48, 0), (14, 10, 46, 0), (15, 4500, 54, 0), (16, 10, 48, 0), (17, 10, 47, 0), (19, 10, 34, 0), (19, 20, 48, 0), (20, 10, 32, 0), (20, 30, 51, 0), (24, 300, 47, 120), (25, 10, 50, 0), (29, 10, 34, 0), (29, 30, 51, 0), (32, 600, 58, 800), (37, 1200, 43, 800), (35, 100, 43, 0)], [(1, 100, 54, 0), (7, 300, 50, 0), (10, 10, 49, 0), (8, 30, 50, 0), (9, 20, 43, 0), (8, 30, 49, 0), (10, 10, 49, 0), (11, 10, 48, 0), (12, 180, 33, 150), (12, 120, 49, 150), (13, 10, 48, 0), (14, 10, 46, 0), (15, 750, 49, 0), (16, 10, 48, 0), (17, 10, 47, 0), (19, 10, 48, 0), (20, 10, 51, 0), (24, 210, 47, 120), (25, 10, 50, 0), (29, 10, 46, 0), (32, 600, 53, 800)], [(1, 100, 43, 0), (2, 600, 46, 200), (3, 45, 51, 0), (5, 15, 47, 0), (6, 10, 55, 0), (12, 180, 49, 150), (13, 10, 54, 0), (8, 10, 50, 0), (9, 120, 36, 0), (9, 120, 43, 0), (8, 10, 49, 0), (10, 5, 49, 0), (11, 5, 48, 0), (12, 180, 49, 150), (13, 10, 52, 0), (15, 4200, 49, 0), (16, 20, 48, 0), (17, 5, 34, 0), (17, 10, 47, 0), (19, 10, 34, 0), (19, 10, 48, 0), (20, 10, 32, 0), (20, 20, 56, 0), (27, 1200, 47, 180), (28, 10, 33, 0), (28, 20, 51, 0), (25, 10, 48, 0), (26, 540, 46, 120), (25, 10, 50, 0), (29, 10, 53, 0), (31, 780, 58, 800), (37, 1200, 43, 800), (35, 100, 43, 0)], [(1, 100, 43, 0), (2, 300, 47, 200), (4, 900, 48, 0), (5, 15, 47, 0), (6, 15, 55, 0), (12, 60, 49, 150), (13, 10, 48, 0), (14, 10, 46, 0), (15, 2400, 54, 0), (16, 10, 48, 0), (17, 10, 47, 0), (19, 10, 48, 0), (20, 10, 51, 0), (24, 600, 47, 120), (25, 10, 50, 0), (29, 10, 46, 0), (32, 1200, 56, 800), (35, 100, 56, 0)], [(1, 100, 54, 0), (7, 300, 50, 0), (10, 10, 49, 0), (11, 10, 48, 0), (12, 120, 49, 150), (13, 10, 48, 0), (14, 10, 46, 0), (15, 3600, 54, 0), (16, 10, 40, 0), (16, 10, 48, 0), (17, 10, 39, 0), (17, 30, 47, 0), (19, 10, 39, 0), (19, 30, 48, 0), (18, 600, 43, 0), (19, 30, 48, 0)], [(1, 100, 54, 0), (7, 240, 56, 0), (11, 30, 34, 0), (11, 30, 48, 0), (12, 120, 49, 150), (13, 10, 48, 0), (14, 10, 46, 0), (15, 4500, 54, 0), (16, 10, 48, 0), (17, 10, 47, 0), (19, 10, 34, 0), (19, 20, 48, 0), (20, 10, 32, 0), (20, 30, 51, 0), (24, 300, 47, 120), (25, 10, 50, 0), (29, 10, 34, 0), (29, 30, 51, 0), (32, 600, 58, 800), (37, 1200, 43, 800), (35, 100, 43, 0)], [(1, 100, 43, 0), (2, 300, 47, 200), (4, 600, 48, 0), (5, 10, 47, 0), (6, 10, 55, 0), (12, 60, 49, 150), (13, 10, 48, 0), (14, 10, 46, 0), (15, 4200, 54, 0), (16, 10, 48, 0), (17, 10, 47, 0), (19, 10, 48, 0), (20, 10, 56, 0), (27, 900, 47, 180), (28, 20, 51, 0), (25, 10, 48, 0), (26, 600, 46, 120), (25, 10, 50, 0), (29, 10, 53, 0), (31, 900, 50, 800), (35, 100, 50, 0)], [(1, 100, 54, 0), (7, 300, 55, 0), (10, 10, 54, 0), (11, 10, 53, 0), (12, 60, 49, 150), (13, 5, 48, 0), (14, 5, 46, 0), (15, 3000, 54, 0), (16, 10, 35, 0), (16, 10, 48, 0), (17, 10, 34, 0), (17, 10, 47, 0), (19, 10, 48, 0), (20, 10, 51, 0), (24, 600, 47, 120), (25, 10, 50, 0), (29, 10, 46, 0), (32, 900, 56, 800), (35, 100, 56, 0)], [(1, 100, 43, 0), (2, 300, 47, 200), (4, 300, 53, 0), (5, 10, 47, 0), (6, 10, 70, 0), (12, 180, 49, 150), (13, 10, 54, 0), (8, 10, 50, 0), (9, 240, 53, 0), (8, 30, 49, 0), (10, 5, 49, 0), (12, 180, 49, 150), (13, 10, 47, 0), (15, 2700, 54, 0), (16, 10, 48, 0), (17, 10, 47, 0), (19, 10, 48, 0), (20, 10, 56, 0), (27, 900, 47, 180), (28, 40, 51, 0), (25, 10, 43, 0), (26, 540, 46, 120), (25, 10, 50, 0), (29, 10, 53, 0), (31, 1200, 58, 800), (37, 960, 43, 800), (35, 100, 43, 0)], [(1, 100, 43, 0), (2, 300, 47, 200), (4, 900, 48, 0), (5, 15, 47, 0), (6, 15, 55, 0), (12, 60, 49, 150), (13, 10, 48, 0), (14, 10, 46, 0), (15, 2400, 54, 0), (16, 10, 48, 0), (17, 10, 47, 0), (19, 10, 48, 0), (20, 10, 51, 0), (24, 600, 47, 120), (25, 10, 50, 0), (29, 10, 46, 0), (32, 1200, 56, 800), (35, 100, 56, 0)], [(1, 100, 43, 0), (2, 300, 47, 200), (4, 300, 53, 0), (5, 10, 47, 0), (6, 10, 70, 0), (12, 180, 49, 150), (13, 10, 54, 0), (8, 10, 50, 0), (9, 240, 53, 0), (8, 30, 49, 0), (10, 5, 49, 0), (12, 180, 49, 150), (13, 10, 47, 0), (15, 2700, 54, 0), (16, 10, 48, 0), (17, 10, 47, 0), (19, 10, 48, 0), (20, 10, 56, 0), (27, 900, 47, 180), (28, 40, 51, 0), (25, 10, 43, 0), (26, 540, 46, 120), (25, 10, 50, 0), (29, 10, 53, 0), (31, 1200, 58, 800), (37, 900, 43, 800), (35, 100, 43, 0)]]
    jobs_data_short =  [ ]

    jobs_data_varie=[     
    ]
    
    jobs_data_petit=[[(1, 100, 54, 0), (7, 240, 56, 0), (11, 30, 34, 0), (11, 30, 48, 0), (12, 120, 49, 150), (13, 10, 48, 0), (14, 10, 46, 0), (15, 4500, 54, 0), (16, 10, 48, 0), (17, 10, 47, 0), (19, 10, 34, 0), (19, 20, 48, 0), (20, 10, 32, 0), (20, 30, 51, 0), (24, 300, 47, 120), (25, 10, 50, 0), (29, 10, 34, 0), (29, 30, 51, 0), (32, 600, 58, 800), (37, 1200, 43, 800), (35, 100, 0, 0)], [(1, 100, 43, 0), (2, 300, 46, 200), (3, 180, 47, 0), (4, 300, 53, 0), (5, 10, 47, 0), (6, 10, 55, 0), (12, 135, 49, 150), (13, 10, 48, 0), (14, 10, 46, 0), (15, 4320, 54, 0), (16, 10, 48, 0), (17, 10, 47, 0), (19, 10, 48, 0), (20, 10, 56, 0), (27, 941, 47, 180), (28, 20, 51, 0), (25, 10, 48, 0), (26, 480, 46, 120), (25, 10, 50, 0), (29, 10, 53, 0), (31, 900, 53, 800), (37, 900, 43, 800), (35, 100, 43, 0)], [(1, 100, 43, 0), (2, 920, 47, 200), (4, 480, 58, 0), (5, 30, 33, 0), (5, 30, 47, 0), (6, 30, 34, 0), (6, 30, 55, 0), (12, 180, 49, 150), (13, 10, 48, 0), (14, 10, 46, 0), (15, 3360, 54, 0), (16, 10, 35, 0), (16, 10, 48, 0), (17, 10, 34, 0), (17, 10, 47, 0), (19, 10, 48, 0), (20, 10, 51, 0), (24, 600, 47, 120), (25, 10, 34, 0), (25, 10, 50, 0), (29, 10, 34, 0), (29, 10, 46, 0), (32, 1200, 56, 800), (35, 100, 56, 0)]]
    
    jobs_data_un=[[(1, 100, 54, 0), (7, 240, 56, 0), (11, 30, 34, 0), (11, 30, 48, 0), (12, 120, 49, 150), (13, 10, 48, 0), (14, 10, 46, 0), (15, 4500, 54, 0), (16, 10, 48, 0), (17, 10, 47, 0), (19, 10, 34, 0), (19, 20, 48, 0), (20, 10, 32, 0), (20, 30, 51, 0), (24, 300, 47, 120), (25, 10, 50, 0), (29, 10, 34, 0), (29, 30, 51, 0), (32, 600, 58, 800), (37, 1200, 43, 800), (35, 100, 0, 0)]]
    jobs_data=jobs_data_big
    print("nb jobs:"+str(len(jobs_data)))
    
    machines_count = 1 + max(task[0] for job in jobs_data for task in job)
    all_machines = range(machines_count)
    # Computes horizon dynamically as the sum of all durations.
    horizon = sum(task[1]+task[3]+task[2] for job in jobs_data for task in job)
    
    # Create the model.
    model = cp_model.CpModel()

   

    # Creates job intervals and add to the corresponding machine lists.
    all_tasks = {}   
    machine_to_intervals = collections.defaultdict(list)
    TPS_C15=0  
    #stat_overlap_zones(jobs_data)

    for job_id, job in enumerate(jobs_data):
        
        for task_id, task in enumerate(job):
            
            machine, duration,tpsDep,derive = task
           
            #PLAY HERE
            if (machine==15):
                TPS_C15+=duration

          
            if(duration <consts.ALLOW_EXTRA_JOB_MOVE_TIME and derive>0):
                if(duration+ derive >= consts.ALLOW_EXTRA_JOB_MOVE_TIME):
                    derive-=consts.ALLOW_EXTRA_JOB_MOVE_TIME-duration
                    duration=consts.ALLOW_EXTRA_JOB_MOVE_TIME
                        

            suffix = f"_{job_id}_{task_id}"
            start_var = model.new_int_var(0, horizon, "start" + suffix)
            end_var = model.new_int_var(0, horizon, "end" + suffix)
            end_var_drift = model.new_int_var(0, horizon, "end drift" + suffix)
            interval_var = model.new_interval_var(start_var, duration+tpsDep, end_var, "interval" + suffix)
            intervalDrift=model.new_interval_var(start_var, duration+tpsDep+derive, end_var_drift, "interval drift" + suffix)
            all_tasks[job_id, task_id] = task_type(
                start=start_var, end=end_var, interval=interval_var,
                intervalDrift=intervalDrift,endDrift=end_var_drift,
                machine=machine,
                duration=duration,derive=derive,tpsDep=tpsDep
            )
           
        
    
    
   
    for job_id, tasks in enumerate(jobs_data):
        for task_id in range(len(tasks) - 1):
            suffix = f"_{job_id}_{task_id}"
            task=all_tasks[job_id, task_id]
            taskNext=all_tasks[job_id, task_id+1]
         
            model.add(taskNext.start <= task.endDrift)
            model.add(taskNext.start >= task.end)

            interval_var=model.new_interval_var(task.start, model.new_int_var(0, horizon, "duree reelle" + suffix), 
                        taskNext.start, "interval reel" + suffix)
            machine_to_intervals[task.machine].append(interval_var)
   

    bridgesMoves(model,jobs_data,machine_to_intervals,all_tasks,horizon)

    set_no_overlap(all_machines,machine_to_intervals,model)

    makePenalties (model,jobs_data,all_tasks)

    # Creates the solver and solve.
    solver = cp_model.CpSolver()
    # Makespan objective.
    makespan = model.new_int_var(0, horizon, "makespan")
    model.add_max_equality(
        makespan,
        [all_tasks[job_id, len(job) - 1].end for job_id, job in enumerate(jobs_data)],
    )
    
   
    if PENALTIES:
        model.Minimize(makespan+sum(penalties))
    else:
        model.Minimize(makespan)

   
    
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
                        duration=all_tasks[job_id, task_id].duration,
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
        #noOverlapGantt (solver)

        # Finally print the solution found.
        
        #print(output)
        print(f"Last unload: {assigned_jobs[35][len(assigned_jobs[35])-1].start}")
        print(f"Optimal Schedule Length: {solver.objective_value}")
        print(f"Temps en C15: {TPS_C15}")
        print(f"Temps en cuves total: {horizon}")       
       

        # Statistics.
        print("\nStatistics")
        print(f"  - conflicts: {solver.num_conflicts}")
        print(f"  - branches : {solver.num_branches}")
        print(f"  - wall time: {solver.wall_time}s")
    else :
        print(f"NO SOLUTION")


def stat_overlap_zones(jobs_data) -> None:
    all_overlap_zones ={}
    all_no_overlap_zones ={}

   

    for job_id, job in enumerate(jobs_data):
        all_overlap_zones[job_id,0] =[]
        all_overlap_zones[job_id,1] =[]
        all_no_overlap_zones[job_id,0] =[]
        all_no_overlap_zones[job_id,1] =[]
        last_id=-2
        bridge=0
        for task_id, task in enumerate(job):            
            machine, duration,tpsDep,derive = task           
           
            if(machine in [1,15,35]): continue
            if machine>=15 : bridge=1
           
            
            
            if(duration <consts.ALLOW_EXTRA_JOB_MOVE_TIME ):
                if ( last_id+1==task_id):
                    all_no_overlap_zones[job_id,bridge][-1]+=duration+tpsDep
                else :
                    all_no_overlap_zones[job_id,bridge].append(60+duration+tpsDep)
                    last_id=task_id
            else :  
                all_overlap_zones[job_id,bridge].append(task_id)              
            
    
    # Récupérer toutes les valeurs uniques de bridge
    bridges = set(bridge for (_, bridge) in all_no_overlap_zones.keys())

    # Boucler sur chaque valeur de bridge
    sumNoOver={}
    sumOver={}
    nbNoOver={}
    nbOver={}
    for bridge in bridges:
        #print(f"Bridge: {bridge}")
        sumNoOver[bridge]=0
        sumOver[bridge]=0
        nbNoOver[bridge]=0
        nbOver[bridge]=0
        for (job_id, b), durations in all_no_overlap_zones.items():
            if b == bridge:
                durations.sort() 
                sumNoOver[bridge]+=sum(durations)
                nbNoOver[bridge]+=len(durations)
                #print(durations)

        for (job_id,b), taskids in all_overlap_zones.items():      
            if b == bridge:
                nbOver[bridge]+=len(taskids)
                for task in taskids:
                    sumOver[bridge]+=jobs_data[job_id][task][1]-60
                    #print(jobs_data[job_id][task][1])

    
    
    print("sumNoOver[0]="+ str(sumNoOver[0]))
    print("sumNoOver[1]="+ str(sumNoOver[1]))
    print("sumOver[0]="+ str(sumOver[0]))
    print("sumOver[1]="+ str(sumOver[1]))

    print("nbNoOver[0]="+ str(nbNoOver[0]))
    print("nbNoOver[1]="+ str(nbNoOver[1]))
    print("nbOver[0]="+ str(nbOver[0]))
    print("nbOver[1]="+ str(nbOver[1]))

def makePenalties (model,jobs_data,all_tasks) -> None:

    for job_id, job in enumerate(jobs_data):
        
        cpt=0
        for task_id in range(len(job) - 1):
            task=all_tasks[job_id, task_id]
            # Ajouter la contrainte conditionnelle
            tps=task.duration+task.derive
            cst=consts.ALLOW_EXTRA_JOB_MOVE_TIME
            if(tps>cst):  cpt+=1
            #cpt+task.duration  
           
        long_task_count[job_id] = cpt

    for job_id, tasks in enumerate(jobs_data):
        for job_id2, tasks2 in enumerate(jobs_data):
            if job_id == job_id2:
                continue
            
            # literal is true if task_b is a direct successor of task_b
            penalty = model.NewBoolVar('penalty')
            task_a_start=all_tasks[job_id,0].start
            task_b_start=all_tasks[job_id2,0].start
            valueA=long_task_count[job_id]
            valueB=long_task_count[job_id2]
            #JOB WITH LESS OVERLAP first
            if(valueA>valueB+2):
                model.Add(task_a_start < task_b_start ).OnlyEnforceIf( penalty)
                model.Add(task_a_start > task_b_start ).OnlyEnforceIf( penalty.Not())
            if(valueA<valueB-2):
                model.Add(task_a_start > task_b_start ).OnlyEnforceIf( penalty)
                model.Add(task_a_start < task_b_start ).OnlyEnforceIf( penalty.Not())
            
        
            penalties.append(penalty)
           
def noOverlapGantt (solver) -> None:
    nooverlap_jobs = collections.defaultdict(list)
    for jobid, machines in NOOVERLAP_JOBS.items():
        for machine, inters in machines.items():
            cpt=0
            for inter  in inters:
                
                nooverlap_jobs[machine].append(
                    assigned_task_type(
                        start=solver.value(inter.StartExpr()),
                        job=jobid,
                        index=cpt,
                        duration=solver.value(inter.EndExpr())-
                            solver.value(inter.StartExpr()),
                        machine=machine
                    )
                )
                cpt+=1

    gantt(nooverlap_jobs)

def set_no_overlap(all_machines,machine_to_intervals,model) ->None:
    cumulMachines32=[]
    # Create and add disjunctive constraints.
    for machine in all_machines:

        match machine:
            case consts.MIDDLE_MACHINE:
                capacity = 3  # Maximum capacity of 2 machines in use at any time 
                model.add_cumulative(
                    machine_to_intervals[machine],
                    [1] * len(machine_to_intervals[machine]),  # Demand of 1 for every interval
                    capacity)
            case 1:
                capacity = 2  # Maximum capacity of 2 machines in use at any time 
                model.add_cumulative(
                    machine_to_intervals[machine],
                    [1] * len(machine_to_intervals[machine]),  # Demand of 1 for every interval
                    capacity)
          
          
           
            case 32|31|30:
                cumulMachines32.append(machine_to_intervals[machine])
                if( machine==30 or machine==31):
                    model.add_no_overlap(machine_to_intervals[machine])   
            case _:
                model.add_no_overlap(machine_to_intervals[machine])    
    
    
    #for machine 32 ...
    liste_aplatie = [item for sous_liste in cumulMachines32 for item in sous_liste]
    model.add_cumulative(liste_aplatie,[1] * len(liste_aplatie),  2)

    print("machine 32="+str(len(liste_aplatie)))
    print("machine 15="+str(len(machine_to_intervals[15])))
    print("machine 1="+str(len(machine_to_intervals[1])))

def  bridgesMoves (model,jobs_data,machine_to_intervals,all_tasks,horizon) ->None:
    #simulate bridge moves per area
    brigesMoves= collections.defaultdict(list)
    # when bridge 1 put down on machine 15
    # bridge 2 must be far away
    
    securityZonesP1P2=[]
    for job_id, job in enumerate(jobs_data):
        NOOVERLAP_JOBS[job_id]={}
        for task_id in range(len(job) ):
            
            task=all_tasks[job_id, task_id ]
            if task.machine not in NOOVERLAP_JOBS[job_id]: NOOVERLAP_JOBS[job_id][task.machine]=[]
            
            
            bridge=0
            suffix = f"_{job_id}_{task_id}"
            if(task.machine >=15) : bridge=1

            if(task.machine in [13,14,16,17] ) :
            #if(task.machine in [14,16,17] ) :
                securityZonesP1P2.append(task.interval)
                
            if(task.machine ==1 ) :
                start_var_fin = model.new_int_var(0, horizon, "start_var_fin" + suffix)
                interval_var_end = model.new_interval_var(
                    start_var_fin, consts.BRIDGE_MOVE_TIME+task.tpsDep, task.end, "interval end_var_deb" + suffix
                )
                brigesMoves[0].append(interval_var_end)
                continue
            
            if(task.machine ==35 ) :
                start_var_fin = model.new_int_var(0, horizon, "start_var_fin" + suffix)
                end_var_deb=model.new_int_var(0, horizon, "end_var_deb" + suffix)
                interval_var_deb = model.new_interval_var(
                    task.start, consts.BRIDGE_MOVE_TIME, end_var_deb, "interval end_var_deb" + suffix
                )
                brigesMoves[1].append(interval_var_deb)
                continue


            if(task.duration < consts.ALLOW_EXTRA_JOB_MOVE_TIME):               
                brigesMoves[bridge].append(task.interval)
                NOOVERLAP_JOBS[job_id][task.machine].append(task.interval)
            else:               
                start_var_fin = model.new_int_var(0, horizon, "start_var_fin" + suffix)
                end_var_deb=model.new_int_var(0, horizon, "end_var_deb" + suffix)
                interval_var_deb = model.new_interval_var(
                    task.start, consts.BRIDGE_MOVE_TIME, end_var_deb, "interval end_var_deb" + suffix
                )
                interval_var_end = model.new_interval_var(
                    start_var_fin, consts.BRIDGE_MOVE_TIME+task.tpsDep, task.end, "interval end_var_deb" + suffix
                )
                
                NOOVERLAP_JOBS[job_id][task.machine].append(interval_var_end)
                NOOVERLAP_JOBS[job_id][task.machine].append(interval_var_deb)
               
                if(task.machine ==consts.MIDDLE_MACHINE) :           
                  
                    brigesMoves[0].append(interval_var_deb)
                    brigesMoves[1].append(interval_var_end)
                    
                    securityZonesP1P2.append(interval_var_deb)
                    securityZonesP1P2.append(interval_var_end)
                else:
                    brigesMoves[bridge].append(interval_var_deb)
                    brigesMoves[bridge].append(interval_var_end)
                   
                
                    

    for brigeMoves in brigesMoves:
        print("size bridges moves: "+str(len(brigesMoves[brigeMoves])))
        model.add_no_overlap(brigesMoves[brigeMoves])
    
   
    if SECURITY_P1_P2: model.add_no_overlap(securityZonesP1P2)

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

