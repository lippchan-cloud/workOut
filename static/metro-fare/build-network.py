#!/usr/bin/env python3
"""Build network.json from Amap Shanghai subway draw JSON."""

from __future__ import annotations

import json
import math
import re
from collections import defaultdict
from pathlib import Path

HERE = Path(__file__).resolve().parent
RAW = HERE / "amap-raw.json"
OUT = HERE / "network.json"
EXCLUDE = {"磁浮线", "市域机场线", "浦江线"}
AMAP_URL = "https://map.amap.com/service/subway?srhdata=3100_drw_shanghai.json"


def haversine_km(lon1: float, lat1: float, lon2: float, lat2: float) -> float:
    r = 6371.0
    p1, p2 = math.radians(lat1), math.radians(lat2)
    dphi = math.radians(lat2 - lat1)
    dlmb = math.radians(lon2 - lon1)
    a = math.sin(dphi / 2) ** 2 + math.cos(p1) * math.cos(p2) * math.sin(dlmb / 2) ** 2
    return 2 * r * math.asin(math.sqrt(a))


def norm_name(n: str) -> str:
    return n.strip().replace("（", "(").replace("）", ")")


def parse_sl(sl: str) -> tuple[float, float]:
    lon, lat = sl.split(",")
    return float(lon), float(lat)


def line_sort_key(name: str) -> tuple[int, str]:
    m = re.search(r"\d+", name)
    return (int(m.group()) if m else 999, name)


def main() -> None:
    data = json.loads(RAW.read_text(encoding="utf-8"))

    # name -> lon/lat samples + lines
    samples: dict[str, list[tuple[float, float]]] = defaultdict(list)
    line_membership: dict[str, set[str]] = defaultdict(set)
    branch_rows: list[dict] = []

    for line in data["l"]:
        ln = line["ln"]
        if ln in EXCLUDE:
            continue
        ordered: list[str] = []
        for st in line.get("st") or []:
            name = norm_name(st["n"])
            lon, lat = parse_sl(st["sl"])
            samples[name].append((lon, lat))
            line_membership[name].add(ln)
            ordered.append(name)
        branch_rows.append(
            {
                "id": line.get("ls") or line.get("li"),
                "name": ln,
                "color": line.get("cl"),
                "stations": ordered,
            }
        )

    stations: dict[str, dict] = {}
    for name, pts in samples.items():
        lon = sum(p[0] for p in pts) / len(pts)
        lat = sum(p[1] for p in pts) / len(pts)
        stations[name] = {
            "name": name,
            "lon": round(lon, 6),
            "lat": round(lat, 6),
            "lines": sorted(line_membership[name]),
        }

    edges: dict[frozenset[str], float] = {}
    edge_lines: dict[frozenset[str], set[str]] = defaultdict(set)
    for branch in branch_rows:
        ordered = branch["stations"]
        ln = branch["name"]
        for a, b in zip(ordered, ordered[1:]):
            if a == b:
                continue
            sa, sb = stations[a], stations[b]
            km = haversine_km(sa["lon"], sa["lat"], sb["lon"], sb["lat"])
            key = frozenset((a, b))
            edge_lines[key].add(ln)
            if key not in edges or km < edges[key]:
                edges[key] = km

    by_name: dict[str, list[dict]] = defaultdict(list)
    for row in branch_rows:
        by_name[row["name"]].append(row)

    ui_lines = []
    for name, parts in by_name.items():
        seen: list[str] = []
        for p in sorted(parts, key=lambda x: -len(x["stations"])):
            for st in p["stations"]:
                if st not in seen:
                    seen.append(st)
        ui_lines.append(
            {
                "name": name,
                "color": parts[0].get("color"),
                "stations": seen,
                "branches": len(parts),
            }
        )
    ui_lines.sort(key=lambda x: line_sort_key(x["name"]))

    station_list = sorted(stations.values(), key=lambda s: s["name"])
    edge_list = [
        {
            "a": a,
            "b": b,
            "km": round(km, 3),
            "lines": sorted(edge_lines.get(key, [])),
        }
        for key, km in edges.items()
        for a, b in [tuple(sorted(key))]
    ]
    edge_list.sort(key=lambda e: (e["a"], e["b"]))

    out = {
        "source": AMAP_URL,
        "fetchedNote": (
            "Interval km estimated by haversine between adjacent station coordinates; "
            "not official operating mileage."
        ),
        "excludedLines": sorted(EXCLUDE),
        "stations": station_list,
        "lines": ui_lines,
        "edges": edge_list,
    }
    OUT.write_text(json.dumps(out, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"wrote {OUT} stations={len(station_list)} lines={len(ui_lines)} edges={len(edge_list)}")


if __name__ == "__main__":
    main()
