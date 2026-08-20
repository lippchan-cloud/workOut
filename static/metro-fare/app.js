import { lookupFares } from "./fare.js";

const $ = (sel) => document.querySelector(sel);

const state = {
  network: null,
  graph: null,
};

function buildGraph(network) {
  const adj = new Map();
  const ensure = (n) => {
    if (!adj.has(n)) adj.set(n, []);
    return adj.get(n);
  };
  for (const e of network.edges) {
    ensure(e.a).push({ to: e.b, km: e.km, lines: e.lines || [] });
    ensure(e.b).push({ to: e.a, km: e.km, lines: e.lines || [] });
  }
  return adj;
}

function dijkstra(graph, start, end) {
  if (!graph.has(start) || !graph.has(end)) return null;
  if (start === end) return { km: 0, path: [start], hops: [] };

  const dist = new Map([[start, 0]]);
  const prev = new Map();
  const prevEdge = new Map();
  const pq = [[0, start]];

  while (pq.length) {
    pq.sort((a, b) => a[0] - b[0]);
    const [d, u] = pq.shift();
    if (d !== dist.get(u)) continue;
    if (u === end) break;
    for (const edge of graph.get(u) || []) {
      const nd = d + edge.km;
      if (!dist.has(edge.to) || nd < dist.get(edge.to)) {
        dist.set(edge.to, nd);
        prev.set(edge.to, u);
        prevEdge.set(edge.to, edge);
        pq.push([nd, edge.to]);
      }
    }
  }

  if (!dist.has(end)) return null;

  const path = [];
  const hops = [];
  for (let cur = end; cur; cur = prev.get(cur)) {
    path.push(cur);
    if (prevEdge.has(cur)) hops.push(prevEdge.get(cur));
    if (cur === start) break;
  }
  path.reverse();
  hops.reverse();
  return { km: dist.get(end), path, hops };
}

function summarizePath(result) {
  if (!result || result.path.length < 2) return "";
  const segments = [];
  let curLine = null;
  let from = result.path[0];
  let last = from;

  const pickLine = (lines, prefer) => {
    if (prefer && lines.includes(prefer)) return prefer;
    return lines[0] || "未知线路";
  };

  for (let i = 0; i < result.hops.length; i++) {
    const hop = result.hops[i];
    const next = result.path[i + 1];
    const line = pickLine(hop.lines, curLine);
    if (curLine == null) curLine = line;
    if (line !== curLine) {
      segments.push(`${curLine} ${from}→${last}`);
      from = last;
      curLine = line;
    }
    last = next;
  }
  segments.push(`${curLine} ${from}→${last}`);
  return segments.join(" · 换 ");
}

function fillLineSelect(selectEl) {
  selectEl.innerHTML = "";
  for (const line of state.network.lines) {
    const opt = document.createElement("option");
    opt.value = line.name;
    opt.textContent = line.name;
    selectEl.appendChild(opt);
  }
}

function fillStationSelect(lineSelect, stationSelect) {
  const lineName = lineSelect.value;
  const line = state.network.lines.find((l) => l.name === lineName);
  const prev = stationSelect.value;
  stationSelect.innerHTML = "";
  if (!line) return;
  for (const name of line.stations) {
    const opt = document.createElement("option");
    opt.value = name;
    opt.textContent = name;
    stationSelect.appendChild(opt);
  }
  if (prev && line.stations.includes(prev)) {
    stationSelect.value = prev;
  }
}

function formatDelta(d) {
  if (d === 0) return "相对现状不变";
  if (d > 0) return `相对现状 +${d} 元`;
  return `相对现状 ${d} 元`;
}

function render() {
  const from = $("#fromStation").value;
  const to = $("#toStation").value;
  const box = $("#results");

  if (!from || !to) {
    box.innerHTML = `<p class="muted">请选择起点与终点。</p>`;
    return;
  }
  if (from === to) {
    box.innerHTML = `<p class="error">起点与终点相同，请重新选择。</p>`;
    return;
  }

  const route = dijkstra(state.graph, from, to);
  if (!route) {
    box.innerHTML = `<p class="error">路网中不可达（数据可能不完整）。</p>`;
    return;
  }

  const fares = lookupFares(route.km);
  const pathText = summarizePath(route);
  const beyond = fares.beyondTable
    ? `<p class="muted">乘距超过对照表常见上限（124km），仍按规则公式计费。</p>`
    : "";

  box.innerHTML = `
    <div class="km-line">
      <span class="muted">估算最短乘距</span>
      <strong>${route.km.toFixed(1)} km</strong>
    </div>
    <div class="fares">
      <div class="fare-card current">
        <div class="tag">现状</div>
        <div class="price">${fares.current}<span style="font-size:.9rem"> 元</span></div>
      </div>
      <div class="fare-card s1">
        <div class="tag">方案一</div>
        <div class="price">${fares.scheme1}<span style="font-size:.9rem"> 元</span></div>
        <div class="delta">${formatDelta(fares.delta1)}</div>
      </div>
      <div class="fare-card s2">
        <div class="tag">方案二</div>
        <div class="price">${fares.scheme2}<span style="font-size:.9rem"> 元</span></div>
        <div class="delta">${formatDelta(fares.delta2)}</div>
      </div>
    </div>
    <div class="path-box"><strong>路径摘要</strong><br>${pathText}</div>
    ${beyond}
  `;
}

function wire() {
  const pairs = [
    ["#fromLine", "#fromStation"],
    ["#toLine", "#toStation"],
  ];
  for (const [lineSel, stSel] of pairs) {
    const line = $(lineSel);
    const st = $(stSel);
    line.addEventListener("change", () => {
      fillStationSelect(line, st);
      render();
    });
    st.addEventListener("change", render);
  }

  $("#swapBtn").addEventListener("click", () => {
    const fl = $("#fromLine").value;
    const tl = $("#toLine").value;
    const fs = $("#fromStation").value;
    const ts = $("#toStation").value;
    $("#fromLine").value = tl;
    $("#toLine").value = fl;
    fillStationSelect($("#fromLine"), $("#fromStation"));
    fillStationSelect($("#toLine"), $("#toStation"));
    $("#fromStation").value = ts;
    $("#toStation").value = fs;
    render();
  });
}

async function main() {
  const res = await fetch("./network.json");
  state.network = await res.json();
  state.graph = buildGraph(state.network);

  fillLineSelect($("#fromLine"));
  fillLineSelect($("#toLine"));
  fillStationSelect($("#fromLine"), $("#fromStation"));
  fillStationSelect($("#toLine"), $("#toStation"));

  const l1 = state.network.lines.find((l) => l.name === "1号线");
  const l2 = state.network.lines.find((l) => l.name === "2号线");
  if (l1?.stations.includes("人民广场")) {
    $("#fromLine").value = "1号线";
    fillStationSelect($("#fromLine"), $("#fromStation"));
    $("#fromStation").value = "人民广场";
  }
  if (l2?.stations.includes("陆家嘴")) {
    $("#toLine").value = "2号线";
    fillStationSelect($("#toLine"), $("#toStation"));
    $("#toStation").value = "陆家嘴";
  }

  wire();
  render();
}

main().catch((err) => {
  $("#results").innerHTML = `<p class="error">加载路网失败：${err.message}</p>`;
});
