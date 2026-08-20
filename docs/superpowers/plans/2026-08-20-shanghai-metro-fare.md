# Shanghai Metro Fare Page Implementation Plan

> **For agentic workers:** Implement task-by-task. Steps use checkbox (`- [ ]`) syntax.

**Goal:** Standalone static page to pick Shanghai metro start/end stations and show current vs scheme1/scheme2 fares from the published distance table.

**Architecture:** Pure frontend under `static/metro-fare/`. Build `network.json` from Amap public subway draw data (station order + lat/lng → haversine edge km). Page runs Dijkstra + fare bracket lookup locally.

**Tech Stack:** HTML/CSS/vanilla JS, no build step, no backend.

## Global Constraints

- No AppShell / auth / Spring integration
- Main network only (exclude maglev / Jinshan if present in source)
- Fares are estimates; show disclaimer
- Full fare brackets from the adjustment chart, not summary approximations

---

## File Structure

- `static/metro-fare/index.html` — UI
- `static/metro-fare/app.js` — selection, Dijkstra, fare lookup, render
- `static/metro-fare/styles.css` — layout
- `static/metro-fare/network.json` — lines, stations, edges
- `static/metro-fare/build-network.py` — one-shot converter from Amap raw JSON
- `static/metro-fare/fare.js` — fare table + lookup (testable)

---

### Task 1: Build network.json from Amap data

- [ ] Parse Amap `l` / `st` structure
- [ ] Filter non-main lines (磁浮、金山等)
- [ ] Build unique stations by normalized name; edges between consecutive stops with haversine km
- [ ] Write `network.json` + data provenance note

### Task 2: Fare lookup + Dijkstra

- [ ] Encode full fare brackets from chart
- [ ] Dijkstra shortest path by km
- [ ] Path summary for UI

### Task 3: Static page UI

- [ ] Line → station pickers for origin/destination
- [ ] Live results: km, three fares, deltas, path, disclaimer

### Task 4: Smoke verify

- [ ] Same-station / unreachable handling
- [ ] Spot-check fare brackets and one cross-line path
