const API_BASE = "/api/extension";
const CUSTOM_MAX = 200;
const DEFAULT_FIXED = ["bat", "cmd", "com", "cpl", "exe", "scr", "js"];

const els = {
  fixedList: document.getElementById("fixed-list"),
  fixedInput: document.getElementById("fixed-input"),
  fixedAdd: document.getElementById("fixed-add"),
  customInput: document.getElementById("custom-input"),
  customAdd: document.getElementById("custom-add"),
  customChips: document.getElementById("custom-chips"),
  customCount: document.getElementById("custom-count"),
  status: document.getElementById("status"),
};

function normalizeExtension(raw) {
  if (raw == null) return "";
  let v = String(raw).trim();
  if (v.startsWith(".")) v = v.slice(1);
  return v.toLowerCase();
}

function setStatus(message, tone = "muted") {
  if (!els.status) return;
  els.status.textContent = message ?? "";
  const color =
    tone === "error" ? "#b91c1c" :
    tone === "ok" ? "#065f46" :
    "#6b7280";
  els.status.style.color = color;
}

async function api(path, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(options.headers ?? {}),
  };

  const res = await fetch(path, { ...options, headers });

  if (res.status === 204) return null;

  const contentType = res.headers.get("content-type") || "";
  const isJson = contentType.includes("application/json");
  const body = isJson ? await res.json().catch(() => null) : await res.text().catch(() => "");

  if (!res.ok) {
    const msg = (body && typeof body === "object" && body.message) ? body.message : (typeof body === "string" ? body : "Request failed");
    const err = new Error(msg);
    err.status = res.status;
    err.body = body;
    throw err;
  }

  return body;
}

function renderFixed(policies) {
  const byExt = new Map(policies.map((p) => [p.extension, p]));

  const extra = policies
    .map((p) => p.extension)
    .filter((ext) => !DEFAULT_FIXED.includes(ext))
    .sort((a, b) => a.localeCompare(b));

  const ordered = [...DEFAULT_FIXED, ...extra];

  els.fixedList.innerHTML = "";

  for (const ext of ordered) {
    const policy = byExt.get(ext);
    // 요구사항: DB에 존재하면 체크 표시
    const checked = !!policy;

    const item = document.createElement("label");
    item.className = "fixedItem";

    const cb = document.createElement("input");
    cb.type = "checkbox";
    cb.checked = checked;
    cb.addEventListener("change", async () => {
      cb.disabled = true;
      setStatus("저장 중...", "muted");

      try {
        if (cb.checked) {
          // 체크 = 생성(차단)
          try {
            await api(`${API_BASE}/fixed`, {
              method: "POST",
              body: JSON.stringify({ extension: ext }),
            });
          } catch (e) {
            // 이미 존재하면(409) 무시하고 UI만 최신화
            if (!e || e.status !== 409) throw e;
          }
          await refreshAll();
          setStatus("저장 완료", "ok");
        } else {
          // 해제 = 삭제
          try {
            await api(`${API_BASE}/fixed/${encodeURIComponent(ext)}`, { method: "DELETE" });
          } catch (e) {
            // 이미 없으면(404) 무시
            if (!e || e.status !== 404) throw e;
          }
          await refreshAll();
          setStatus("저장 완료", "ok");
        }
      } catch (e) {
        cb.checked = !cb.checked;
        setStatus(e.message || "저장 실패", "error");
      } finally {
        cb.disabled = false;
      }
    });

    const txt = document.createElement("span");
    txt.textContent = ext;

    const del = document.createElement("button");
    del.type = "button";
    del.className = "fixedDeleteBtn";
    del.textContent = "×";
    del.title = "삭제";
    del.disabled = !policy;
    del.addEventListener("click", async (ev) => {
      ev.preventDefault();
      ev.stopPropagation();

      if (!policy) return;

      del.disabled = true;
      setStatus("삭제 중...", "muted");
      try {
        await api(`${API_BASE}/fixed/${encodeURIComponent(ext)}`, { method: "DELETE" });
        await refreshAll();
        setStatus("삭제 완료", "ok");
      } catch (e) {
        setStatus(e.message || "삭제 실패", "error");
      } finally {
        del.disabled = false;
      }
    });

    item.appendChild(cb);
    item.appendChild(txt);
    item.appendChild(del);
    els.fixedList.appendChild(item);
  }
}

function renderCustom(extensions) {
  const list = [...extensions].sort((a, b) => a.localeCompare(b));

  els.customCount.textContent = `${list.length}/${CUSTOM_MAX}`;
  els.customChips.innerHTML = "";

  for (const ext of list) {
    const chip = document.createElement("div");
    chip.className = "chip";

    const text = document.createElement("span");
    text.textContent = ext;

    const btn = document.createElement("button");
    btn.className = "chipBtn";
    btn.type = "button";
    btn.textContent = "×";
    btn.title = "삭제";

    btn.addEventListener("click", async () => {
      btn.disabled = true;
      setStatus("삭제 중...", "muted");
      try {
        await api(`${API_BASE}/custom/${encodeURIComponent(ext)}`, { method: "DELETE" });
        await refreshAll();
        setStatus("삭제 완료", "ok");
      } catch (e) {
        setStatus(e.message || "삭제 실패", "error");
      } finally {
        btn.disabled = false;
      }
    });

    // Optional: rename on double click
    chip.addEventListener("dblclick", async () => {
      const nextRaw = prompt("새 확장자 입력", ext);
      if (nextRaw == null) return;

      const next = normalizeExtension(nextRaw);
      if (!next) {
        setStatus("확장자를 입력하세요", "error");
        return;
      }
      if (next === ext) return;

      setStatus("수정 중...", "muted");
      try {
        await api(`${API_BASE}/custom/${encodeURIComponent(ext)}`, {
          method: "PUT",
          body: JSON.stringify({ newExtension: next }),
        });
        await refreshAll();
        setStatus("수정 완료", "ok");
      } catch (e) {
        setStatus(e.message || "수정 실패", "error");
      }
    });

    chip.appendChild(text);
    chip.appendChild(btn);
    els.customChips.appendChild(chip);
  }
}

async function refreshAll() {
  const [fixed, custom] = await Promise.all([
    api(`${API_BASE}/fixed`, { method: "GET" }),
    api(`${API_BASE}/custom`, { method: "GET" }),
  ]);

  renderFixed(Array.isArray(fixed) ? fixed : []);
  renderCustom((Array.isArray(custom) ? custom : []).map((x) => x.extension));
}

async function onAddCustom() {
  const raw = els.customInput.value;
  const ext = normalizeExtension(raw);

  if (!ext) {
    setStatus("확장자를 입력하세요", "error");
    return;
  }

  els.customAdd.disabled = true;
  setStatus("추가 중...", "muted");

  try {
    await api(`${API_BASE}/custom`, {
      method: "POST",
      body: JSON.stringify({ extension: ext }),
    });
    els.customInput.value = "";
    await refreshAll();
    setStatus("추가 완료", "ok");
  } catch (e) {
    setStatus(e.message || "추가 실패", "error");
  } finally {
    els.customAdd.disabled = false;
  }
}

async function onAddFixed() {
  const raw = els.fixedInput.value;
  const ext = normalizeExtension(raw);

  if (!ext) {
    setStatus("확장자를 입력하세요", "error");
    return;
  }

  els.fixedAdd.disabled = true;
  setStatus("추가 중...", "muted");

  try {
    await api(`${API_BASE}/fixed`, {
      method: "POST",
      body: JSON.stringify({ extension: ext }),
    });
    els.fixedInput.value = "";
    await refreshAll();
    setStatus("추가 완료", "ok");
  } catch (e) {
    setStatus(e.message || "추가 실패", "error");
  } finally {
    els.fixedAdd.disabled = false;
  }
}

function wireEvents() {
  els.fixedAdd.addEventListener("click", onAddFixed);
  els.fixedInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter") onAddFixed();
  });

  els.customAdd.addEventListener("click", onAddCustom);
  els.customInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter") onAddCustom();
  });
}

async function main() {
  wireEvents();
  setStatus("불러오는 중...", "muted");

  try {
    await refreshAll();
    setStatus("", "muted");
  } catch (e) {
    setStatus(e.message || "데이터 로드 실패", "error");
  }
}

main();
