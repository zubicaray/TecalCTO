import re
from collections import defaultdict

# Les logs bruts
logs = """
BRIDGE:1 ,previousTpsDep=46, SIMU=23-000105 ,taskid:14 zone:C28 deb:7949, fin=8291
BRIDGE:1 ,previousTpsDep=46, SIMU=23-000105 ,taskid:17 zone:C27 deb:9136, fin=9367
BRIDGE:1 ,previousTpsDep=46, SIMU=23-000105 ,taskid:20 zone:C32 deb:9912, fin=10139
BRIDGE:1 ,previousTpsDep=46, SIMU=23-000105 ,taskid:21 zone:C37 deb:10979, fin=11092
END ZONE, BRIDGE:1 ,previousTpsDep=46, SIMU=23-000105 ,taskid:22 zone:D1 ? D2 deb:12761, fin=12864
END ZONE, BRIDGE:1 ,previousTpsDep=48, SIMU=N2-000601 ,taskid:26 zone:D1 ? D2 deb:8424, fin=8534
BRIDGE:1 ,previousTpsDep=47, SIMU=29-000024 ,taskid:16 zone:C37 deb:8301, fin=8414
END ZONE, BRIDGE:1 ,previousTpsDep=47, SIMU=29-000024 ,taskid:17 zone:D1 ? D2 deb:9724, fin=9827
BRIDGE:1 ,previousTpsDep=44, SIMU=25-000020 ,taskid:15 zone:C25 deb:9377, fin=9714
BRIDGE:1 ,previousTpsDep=44, SIMU=25-000020 ,taskid:20 zone:C31 ? C32 deb:10259, fin=10573
END ZONE, BRIDGE:1 ,previousTpsDep=44, SIMU=25-000020 ,taskid:21 zone:D1 ? D2 deb:12106, fin=12217
BRIDGE:1 ,previousTpsDep=44, SIMU=24-000097 ,taskid:16 zone:C28 deb:11623, fin=12026
BRIDGE:1 ,previousTpsDep=44, SIMU=24-000097 ,taskid:19 zone:C27 deb:13241, fin=13482
BRIDGE:1 ,previousTpsDep=44, SIMU=24-000097 ,taskid:22 zone:C32 deb:14027, fin=14254
END ZONE, BRIDGE:1 ,previousTpsDep=44, SIMU=24-000097 ,taskid:23 zone:D1 ? D2 deb:15399, fin=15509
BRIDGE:0 ,previousTpsDep=42, SIMU=26-000485 ,taskid:5 zone:C10 deb:8205, fin=8433
BRIDGE:0 ,previousTpsDep=42, SIMU=26-000485 ,taskid:8 zone:C13 ? C15 deb:8558, fin=8781
BRIDGE:1 ,previousTpsDep=42, SIMU=26-000485 ,taskid:12 zone:C25 deb:11246, fin=11563
BRIDGE:1 ,previousTpsDep=42, SIMU=26-000485 ,taskid:15 zone:C31 ? C32 deb:12227, fin=12453
END ZONE, BRIDGE:1 ,previousTpsDep=42, SIMU=26-000485 ,taskid:16 zone:D1 ? D2 deb:13492, fin=13603
END ZONE, BRIDGE:0 ,previousTpsDep=52, SIMU=19-000152 ,taskid:7 zone:CHGT1 ? CHGT2 deb:7994, fin=8129
"""

# Extraction des informations des logs
pattern = r"BRIDGE:(\d+) .* SIMU=([\w-]+) .* deb:(\d+), fin=(\d+)"
matches = re.findall(pattern, logs)

# Organisation des données par pont
bridges = defaultdict(list)
for bridge, simu, deb, fin in matches:
    bridges[bridge].append({"simu": simu, "deb": int(deb), "fin": int(fin)})

# Détection des chevauchements
def detect_overlaps(logs):
    overlaps = []
    for i, log1 in enumerate(logs):
        for j, log2 in enumerate(logs):
            # Détection des chevauchements (stricts, pas d'adjacence)
            if i < j and log1["fin"] > log2["deb"] and log1["deb"] < log2["fin"]:
                overlaps.append({
                    "simu1": log1["simu"],
                    "deb1": log1["deb"],
                    "fin1": log1["fin"],
                    "simu2": log2["simu"],
                    "deb2": log2["deb"],
                    "fin2": log2["fin"]
                })
    return overlaps

# Analyse des chevauchements pour chaque pont
results = {}
for bridge, logs in bridges.items():
    results[bridge] = detect_overlaps(sorted(logs, key=lambda x: x["deb"]))  # Trier par `deb`

# Affichage des résultats
for bridge, overlaps in results.items():
    print(f"BRIDGE:{bridge}")
    if overlaps:
        for overlap in overlaps:
            print(f"  Chevauchement entre {overlap['simu1']} (deb={overlap['deb1']}, fin={overlap['fin1']}) "
                  f"et {overlap['simu2']} (deb={overlap['deb2']}, fin={overlap['fin2']})")
    else:
        print("  Aucun chevauchement détecté.")


