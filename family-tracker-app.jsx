import { useState, useEffect, useRef, useCallback } from "react";

/* ═══════════════════════════════════════════
   HAVEN — Family Safety Reimagined
   Aesthetic: Organic Soft Brutalism + Bento
   ═══════════════════════════════════════════ */

const TH = {
  sand: {
    name: "Sand", emoji: "🏜️",
    bg: "#f5f0e8", bgSub: "#ece5d8", surface: "#fff", surfaceAlt: "#faf7f2",
    card: "#ffffff", cardAlt: "#f9f5ed",
    accent: "#c2410c", accentSoft: "#fff7ed", accentMid: "#fb923c", accentBg: "rgba(194,65,12,0.07)",
    text: "#1c1917", textMid: "#57534e", textFade: "#a8a29e",
    ok: "#15803d", warn: "#a16207", danger: "#b91c1c",
    border: "rgba(0,0,0,0.06)", borderStrong: "rgba(0,0,0,0.1)",
    shadow: "0 1px 3px rgba(0,0,0,0.04), 0 4px 12px rgba(0,0,0,0.03)",
    shadowLg: "0 8px 32px rgba(0,0,0,0.08)",
    grad: "linear-gradient(135deg, #c2410c, #ea580c)",
    phoneBg: "#d6cfc3", dark: false,
  },
  charcoal: {
    name: "Charcoal", emoji: "🌑",
    bg: "#171717", bgSub: "#1c1c1c", surface: "#222222", surfaceAlt: "#262626",
    card: "#222222", cardAlt: "#2a2a2a",
    accent: "#fb923c", accentSoft: "rgba(251,146,60,0.1)", accentMid: "#fdba74", accentBg: "rgba(251,146,60,0.06)",
    text: "#fafaf9", textMid: "#a8a29e", textFade: "#57534e",
    ok: "#4ade80", warn: "#fbbf24", danger: "#f87171",
    border: "rgba(255,255,255,0.06)", borderStrong: "rgba(255,255,255,0.1)",
    shadow: "0 1px 3px rgba(0,0,0,0.2), 0 4px 12px rgba(0,0,0,0.15)",
    shadowLg: "0 8px 32px rgba(0,0,0,0.4)",
    grad: "linear-gradient(135deg, #ea580c, #fb923c)",
    phoneBg: "#0a0a0a", dark: true,
  },
  ocean: {
    name: "Ocean", emoji: "🌊",
    bg: "#0c1a2e", bgSub: "#0f2035", surface: "#142840", surfaceAlt: "#183050",
    card: "#142840", cardAlt: "#183050",
    accent: "#38bdf8", accentSoft: "rgba(56,189,248,0.08)", accentMid: "#7dd3fc", accentBg: "rgba(56,189,248,0.05)",
    text: "#f0f9ff", textMid: "#7dd3fc", textFade: "#365880",
    ok: "#4ade80", warn: "#fde68a", danger: "#fca5a5",
    border: "rgba(56,189,248,0.08)", borderStrong: "rgba(56,189,248,0.15)",
    shadow: "0 1px 3px rgba(0,0,0,0.3), 0 4px 12px rgba(0,0,0,0.2)",
    shadowLg: "0 8px 32px rgba(0,0,0,0.5)",
    grad: "linear-gradient(135deg, #0284c7, #38bdf8)",
    phoneBg: "#050d18", dark: true,
  },
  flora: {
    name: "Flora", emoji: "🌿",
    bg: "#f0f7f1", bgSub: "#e4efe6", surface: "#ffffff", surfaceAlt: "#f5faf5",
    card: "#ffffff", cardAlt: "#f5faf6",
    accent: "#16a34a", accentSoft: "#f0fdf4", accentMid: "#4ade80", accentBg: "rgba(22,163,74,0.06)",
    text: "#14532d", textMid: "#3f6b50", textFade: "#86b898",
    ok: "#16a34a", warn: "#ca8a04", danger: "#dc2626",
    border: "rgba(0,0,0,0.05)", borderStrong: "rgba(0,0,0,0.09)",
    shadow: "0 1px 3px rgba(0,0,0,0.03), 0 4px 12px rgba(0,0,0,0.03)",
    shadowLg: "0 8px 32px rgba(0,0,0,0.07)",
    grad: "linear-gradient(135deg, #16a34a, #22c55e)",
    phoneBg: "#c8dccb", dark: false,
  },
  dusk: {
    name: "Dusk", emoji: "🌆",
    bg: "#1e1028", bgSub: "#241335", surface: "#2c1a40", surfaceAlt: "#341f4d",
    card: "#2c1a40", cardAlt: "#341f4d",
    accent: "#c084fc", accentSoft: "rgba(192,132,252,0.08)", accentMid: "#d8b4fe", accentBg: "rgba(192,132,252,0.05)",
    text: "#faf5ff", textMid: "#d8b4fe", textFade: "#5c3d80",
    ok: "#4ade80", warn: "#fde68a", danger: "#fca5a5",
    border: "rgba(192,132,252,0.07)", borderStrong: "rgba(192,132,252,0.14)",
    shadow: "0 1px 3px rgba(0,0,0,0.3), 0 4px 12px rgba(0,0,0,0.2)",
    shadowLg: "0 8px 32px rgba(0,0,0,0.5)",
    grad: "linear-gradient(135deg, #9333ea, #c084fc)",
    phoneBg: "#0d0614", dark: true,
  },
};

const FAM = [
  { id: 1, name: "Mom", ini: "M", hue: "#e879a0", bat: 78, addr: "Home", spd: 0, seen: "Now", stat: "Home", ring: 0.3, angle: 30 },
  { id: 2, name: "Dad", ini: "D", hue: "#60a5fa", bat: 45, addr: "350 5th Ave", spd: 28, seen: "2m", stat: "Driving", ring: 0.7, angle: 150 },
  { id: 3, name: "Sarah", ini: "S", hue: "#a78bfa", bat: 92, addr: "NYU Campus", spd: 0, seen: "5m", stat: "School", ring: 0.55, angle: 240 },
  { id: 4, name: "Jake", ini: "J", hue: "#34d399", bat: 23, addr: "Central Park", spd: 3, seen: "1m", stat: "Walking", ring: 0.85, angle: 310 },
  { id: 5, name: "Gran", ini: "G", hue: "#fbbf24", bat: 61, addr: "Brooklyn Hts", spd: 0, seen: "12m", stat: "Home", ring: 0.4, angle: 100 },
];

const PLACES = [
  { id: 1, name: "Home", addr: "142 Oak Ave", count: 3, color: "#e879a0" },
  { id: 2, name: "Office", addr: "350 5th Ave", count: 1, color: "#60a5fa" },
  { id: 3, name: "NYU", addr: "Washington Sq", count: 1, color: "#a78bfa" },
];

const DRIVES = [
  { id: 1, who: "Dad", date: "Today 8:32a", from: "Home", to: "Work", score: 87, dist: "12.4mi", dur: "28m", top: 62, brakes: 1 },
  { id: 2, who: "Mom", date: "Today 7:15a", from: "Home", to: "Store", score: 96, dist: "3.2mi", dur: "11m", top: 38, brakes: 0 },
  { id: 3, who: "Dad", date: "Yesterday", from: "Work", to: "Home", score: 79, dist: "13.1mi", dur: "34m", top: 58, brakes: 2 },
];

const NOTIFS = [
  { id: 1, t: "Mom arrived Home", sub: "2 min ago", c: "#34d399" },
  { id: 2, t: "Jake — battery low", sub: "8 min ago", c: "#f87171" },
  { id: 3, t: "Dad speed alert", sub: "15 min ago", c: "#fbbf24" },
  { id: 4, t: "Sarah left campus", sub: "22 min ago", c: "#a78bfa" },
];

const INIT_MSGS = [
  { id: 1, w: "Mom", t: "Dinner at 7! Making pasta 🍝", ts: "5:32p" },
  { id: 2, w: "Dad", t: "10 min late — bridge traffic", ts: "5:45p" },
  { id: 3, w: "Sarah", t: "Can someone pick me up?", ts: "5:50p" },
  { id: 4, w: "Jake", t: "Heading home from the park", ts: "5:55p" },
  { id: 5, w: "Mom", t: "Dad grab Sarah on your way?", ts: "5:58p" },
  { id: 6, w: "Dad", t: "On it 👍", ts: "6:01p" },
];

const famColor = (n) => FAM.find(f => f.name === n)?.hue || "#999";

/* ── Radial Orbit Map ── */
function OrbitMap({ th, fam, sel, onSel }) {
  const cx = 175, cy = 185;
  const rings = [55, 95, 135, 170];
  return (
    <div style={{ position: "relative", width: "100%", height: "100%", background: th.dark ? th.bgSub : th.bgSub, overflow: "hidden" }}>
      <svg width="100%" height="100%" viewBox="0 0 350 390" style={{ position: "absolute" }}>
        {/* Orbit rings */}
        {rings.map((r, i) => (
          <circle key={i} cx={cx} cy={cy} r={r} fill="none" stroke={th.border} strokeWidth={i === 0 ? 1.5 : 0.8} strokeDasharray={i > 0 ? "4 6" : "none"} />
        ))}
        {/* Crosshair */}
        <line x1={cx} y1={cy - 8} x2={cx} y2={cy + 8} stroke={th.accent} strokeWidth="1.5" opacity="0.4" />
        <line x1={cx - 8} y1={cy} x2={cx + 8} y2={cy} stroke={th.accent} strokeWidth="1.5" opacity="0.4" />
        {/* Grid dots */}
        {Array.from({ length: 8 }).map((_, i) => {
          const a = (i / 8) * Math.PI * 2;
          return <circle key={`g${i}`} cx={cx + Math.cos(a) * 170} cy={cy + Math.sin(a) * 170} r="1.5" fill={th.textFade} opacity="0.3" />;
        })}
      </svg>

      {/* HOME label at center */}
      <div style={{
        position: "absolute", left: cx - 22, top: cy - 22,
        width: 44, height: 44, borderRadius: "50%",
        background: th.accentBg, border: `2px solid ${th.accent}44`,
        display: "flex", alignItems: "center", justifyContent: "center",
      }}>
        <span style={{ fontSize: 10, fontWeight: 800, color: th.accent, fontFamily: "'Space Mono',monospace", letterSpacing: 0.5 }}>HOME</span>
      </div>

      {/* Member nodes */}
      {fam.map(m => {
        const rad = m.ring * 160 + 30;
        const ang = (m.angle * Math.PI) / 180;
        const x = cx + Math.cos(ang) * rad;
        const y = cy + Math.sin(ang) * rad;
        const isSel = sel?.id === m.id;
        return (
          <div key={m.id} onClick={() => onSel(m)} style={{
            position: "absolute",
            left: x - 22, top: y - 22,
            cursor: "pointer", zIndex: isSel ? 20 : 10,
            transition: "transform 0.4s cubic-bezier(0.34,1.56,0.64,1)",
            transform: isSel ? "scale(1.25)" : "scale(1)",
          }}>
            {isSel && (
              <div style={{
                position: "absolute", inset: -8, borderRadius: "50%",
                border: `2px solid ${m.hue}`,
                animation: "orbPulse 1.8s ease-out infinite",
              }} />
            )}
            <div style={{
              width: 44, height: 44, borderRadius: "50%",
              background: isSel ? m.hue : th.surface,
              border: `3px solid ${m.hue}`,
              display: "flex", alignItems: "center", justifyContent: "center",
              boxShadow: isSel ? `0 0 20px ${m.hue}44` : th.shadow,
              transition: "all 0.3s",
            }}>
              <span style={{
                fontSize: 17, fontWeight: 900,
                color: isSel ? "#fff" : m.hue,
                fontFamily: "'Outfit', sans-serif",
              }}>{m.ini}</span>
            </div>
            {/* Speed indicator line */}
            {m.spd > 0 && (
              <div style={{
                position: "absolute", top: -3, right: -3,
                width: 16, height: 16, borderRadius: "50%",
                background: th.warn, border: `2px solid ${th.surface}`,
                display: "flex", alignItems: "center", justifyContent: "center",
                fontSize: 7, fontWeight: 800, color: "#fff",
              }}>{m.spd}</div>
            )}
            {/* Name chip */}
            <div style={{
              position: "absolute", top: 48, left: "50%", transform: "translateX(-50%)",
              background: isSel ? m.hue : th.surface,
              color: isSel ? "#fff" : th.text,
              fontSize: 9, fontWeight: 700, padding: "2px 8px", borderRadius: 8,
              whiteSpace: "nowrap", boxShadow: th.shadow,
              fontFamily: "'Space Mono', monospace",
              letterSpacing: 0.5,
            }}>
              {m.name}
            </div>
          </div>
        );
      })}

      {/* Coord readout */}
      <div style={{
        position: "absolute", bottom: 8, right: 10,
        fontFamily: "'Space Mono',monospace", fontSize: 8,
        color: th.textFade, letterSpacing: 0.8,
        background: `${th.surface}cc`, padding: "2px 6px", borderRadius: 4,
      }}>40.758°N 73.985°W</div>
    </div>
  );
}

/* ── Arc Battery ── */
function Batt({ v, s = 28, th }) {
  const r = (s - 4) / 2, c = Math.PI * 2 * r;
  const col = v > 50 ? th.ok : v > 20 ? th.warn : th.danger;
  return (
    <div style={{ position: "relative", width: s, height: s }}>
      <svg width={s} height={s} style={{ transform: "rotate(-90deg)" }}>
        <circle cx={s/2} cy={s/2} r={r} fill="none" stroke={th.border} strokeWidth="2.5" />
        <circle cx={s/2} cy={s/2} r={r} fill="none" stroke={col} strokeWidth="2.5" strokeDasharray={c} strokeDashoffset={c*(1-v/100)} strokeLinecap="round" />
      </svg>
      <span style={{ position: "absolute", inset: 0, display: "flex", alignItems: "center", justifyContent: "center", fontSize: s*0.3, fontWeight: 700, color: col, fontFamily: "'Space Mono',monospace" }}>{v}</span>
    </div>
  );
}

/* ── Score Arc ── */
function ScoreArc({ v, s = 56, th }) {
  const r = (s-6)/2, c = Math.PI*2*r;
  const col = v >= 90 ? th.ok : v >= 70 ? th.warn : th.danger;
  return (
    <div style={{ position: "relative", width: s, height: s }}>
      <svg width={s} height={s} style={{ transform: "rotate(-90deg)" }}>
        <circle cx={s/2} cy={s/2} r={r} fill="none" stroke={`${col}22`} strokeWidth="4" />
        <circle cx={s/2} cy={s/2} r={r} fill="none" stroke={col} strokeWidth="4" strokeDasharray={c} strokeDashoffset={c*(1-v/100)} strokeLinecap="round" />
      </svg>
      <span style={{ position: "absolute", inset: 0, display: "flex", alignItems: "center", justifyContent: "center", fontSize: s*0.34, fontWeight: 800, color: col, fontFamily: "'Space Mono',monospace" }}>{v}</span>
    </div>
  );
}

/* ── Toggle ── */
function Tog({ on: init, th }) {
  const [on, set] = useState(init);
  return (
    <div onClick={e => { e.stopPropagation(); set(!on); }} style={{
      width: 44, height: 26, borderRadius: 13,
      background: on ? th.accent : th.dark ? th.surfaceAlt : th.bgSub,
      padding: 3, cursor: "pointer",
      display: "flex", alignItems: "center",
      justifyContent: on ? "flex-end" : "flex-start",
      transition: "all 0.3s",
      border: `1px solid ${on ? th.accent : th.border}`,
    }}>
      <div style={{ width: 20, height: 20, borderRadius: "50%", background: "#fff", boxShadow: "0 1px 4px rgba(0,0,0,0.15)", transition: "all 0.25s" }} />
    </div>
  );
}


/* ═══════════════════════════════════════════
   MAIN
   ═══════════════════════════════════════════ */
export default function Haven() {
  const [theme, setTheme] = useState("sand");
  const [tab, setTab] = useState("home");
  const [view, setView] = useState(null);
  const [viewData, setViewData] = useState(null);
  const [mapSel, setMapSel] = useState(null);
  const [sosOpen, setSosOpen] = useState(false);
  const [sosCd, setSosCd] = useState(null);
  const [chatIn, setChatIn] = useState("");
  const [msgs, setMsgs] = useState(INIT_MSGS);
  const [checkin, setCheckin] = useState(false);
  const chatRef = useRef(null);
  const t = TH[theme];
  const sp = { fontFamily: "'Space Mono',monospace" };
  const out = { fontFamily: "'Outfit',sans-serif" };

  useEffect(() => {
    if (sosCd > 0) { const x = setTimeout(() => setSosCd(sosCd-1), 1000); return () => clearTimeout(x); }
    if (sosCd === 0) setTimeout(() => { setSosOpen(false); setSosCd(null); }, 1400);
  }, [sosCd]);

  useEffect(() => { chatRef.current?.scrollIntoView({ behavior: "smooth" }); }, [msgs]);

  const send = useCallback(() => {
    if (!chatIn.trim()) return;
    setMsgs(p => [...p, { id: p.length+1, w: "You", t: chatIn, ts: "Now" }]);
    setChatIn("");
  }, [chatIn]);

  const go = (v, d) => { setView(v); setViewData(d); setCheckin(false); };
  const back = () => { setView(null); setViewData(null); };
  const toTab = (tb) => { setTab(tb); setView(null); setViewData(null); };

  /* Shared */
  const crd = (extra) => ({
    background: t.card, borderRadius: 20, border: `1px solid ${t.border}`,
    boxShadow: t.shadow, cursor: "pointer", transition: "all 0.2s", ...extra,
  });
  const label = { fontSize: 10, fontWeight: 700, color: t.textFade, letterSpacing: 1.5, textTransform: "uppercase", ...sp };

  /* ── Status Bar ── */
  const SBar = () => (
    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "8px 22px 0", fontSize: 12, fontWeight: 700, color: t.text }}>
      <span style={sp}>9:41</span>
      <div style={{ position: "absolute", left: "50%", transform: "translateX(-50%)", width: 80, height: 24, background: "#000", borderRadius: "0 0 20px 20px", top: 4 }} />
      <div style={{ display: "flex", gap: 5, alignItems: "center" }}>
        <svg width="14" height="14" viewBox="0 0 24 24" fill={t.text}><path d="M1 9l11 11L23 9a15 15 0 00-22 0z"/></svg>
        <svg width="14" height="14" viewBox="0 0 24 24" fill={t.text}><rect x="1" y="6" width="4" height="12" rx="1" opacity=".3"/><rect x="7" y="4" width="4" height="14" rx="1" opacity=".55"/><rect x="13" y="2" width="4" height="16" rx="1" opacity=".8"/><rect x="19" y="0" width="4" height="18" rx="1"/></svg>
        <div style={{ width: 24, height: 11, borderRadius: 3, border: `1.5px solid ${t.text}`, padding: 1.5 }}>
          <div style={{ width: "70%", height: "100%", borderRadius: 1.5, background: t.text }} />
        </div>
      </div>
    </div>
  );

  /* ── Header ── */
  const Head = ({ title, bk, right }) => (
    <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", padding: "12px 20px 4px" }}>
      <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
        {bk && <div onClick={back} style={{ cursor: "pointer", width: 32, height: 32, borderRadius: 10, background: t.accentBg, display: "flex", alignItems: "center", justifyContent: "center" }}>
          <svg width="16" height="16" fill="none" viewBox="0 0 24 24"><path d="M15 18l-6-6 6-6" stroke={t.accent} strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"/></svg>
        </div>}
        <span style={{ fontSize: 22, fontWeight: 800, color: t.text, ...out, letterSpacing: -0.5 }}>{title}</span>
      </div>
      {right}
    </div>
  );

  /* ── Bottom Nav ── */
  const Nav = () => {
    const tabs = [
      { id: "home", label: "Home", d: "M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-4 0h4" },
      { id: "map", label: "Radar", d: "M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 1118 0z" },
      { id: "safety", label: "Safety", d: "M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" },
      { id: "chat", label: "Chat", d: "M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" },
      { id: "settings", label: "More", d: "M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.066 2.573c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.573 1.066c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.066-2.573c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" },
    ];
    return (
      <div style={{
        display: "flex", justifyContent: "space-around", alignItems: "center",
        padding: "2px 8px 14px",
        background: t.dark ? `${t.bg}ee` : `${t.bg}ee`,
        backdropFilter: "blur(16px)",
        borderTop: `1px solid ${t.border}`,
      }}>
        {tabs.map(tb => {
          const act = tab === tb.id && !view;
          return (
            <div key={tb.id} onClick={() => toTab(tb.id)} style={{
              display: "flex", flexDirection: "column", alignItems: "center", gap: 2,
              cursor: "pointer", padding: "6px 8px",
            }}>
              <div style={{
                width: 38, height: 30, borderRadius: 12,
                background: act ? t.accentBg : "transparent",
                display: "flex", alignItems: "center", justifyContent: "center",
                transition: "all 0.25s",
              }}>
                <svg width="20" height="20" fill="none" viewBox="0 0 24 24">
                  <path d={tb.d} stroke={act ? t.accent : t.textFade} strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </div>
              <span style={{ fontSize: 9, fontWeight: act ? 800 : 500, color: act ? t.accent : t.textFade, ...sp, letterSpacing: 0.5 }}>{tb.label}</span>
            </div>
          );
        })}
      </div>
    );
  };

  /* ═══ CONTENT ═══ */
  const Content = () => {

    /* ── Notifications ── */
    if (view === "notifs") return (
      <div style={{ flex: 1, overflow: "auto" }}>
        <Head title="Activity" bk />
        <div style={{ padding: "8px 18px 18px", display: "flex", flexDirection: "column", gap: 8 }}>
          {NOTIFS.map((n, i) => (
            <div key={n.id} style={{ ...crd({ padding: "14px 16px", display: "flex", alignItems: "center", gap: 14 }), animation: `hvnSlide 0.35s ease ${i*0.06}s both` }}>
              <div style={{ width: 10, height: 10, borderRadius: "50%", background: n.c, flexShrink: 0, boxShadow: `0 0 10px ${n.c}44` }} />
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 13, color: t.text, fontWeight: 600, ...out }}>{n.t}</div>
                <div style={{ fontSize: 10, color: t.textFade, marginTop: 2, ...sp }}>{n.sub}</div>
              </div>
            </div>
          ))}
        </div>
      </div>
    );

    /* ── Themes ── */
    if (view === "themes") return (
      <div style={{ flex: 1, overflow: "auto" }}>
        <Head title="Appearance" bk />
        <div style={{ padding: "8px 18px 18px", display: "flex", flexDirection: "column", gap: 10 }}>
          {Object.entries(TH).map(([k, v]) => (
            <div key={k} onClick={() => setTheme(k)} style={{
              ...crd({ padding: "16px 18px", display: "flex", alignItems: "center", gap: 16 }),
              border: theme === k ? `2.5px solid ${v.accent}` : `1px solid ${t.border}`,
              background: theme === k ? v.accentSoft || v.accentBg : t.card,
            }}>
              <div style={{ width: 48, height: 48, borderRadius: 14, overflow: "hidden", display: "flex", flexShrink: 0, flexDirection: "column", border: `1px solid ${v.accent}33` }}>
                <div style={{ flex: 1, background: v.bg }} />
                <div style={{ height: 4, background: v.accent }} />
                <div style={{ flex: 0.5, background: v.surface }} />
              </div>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 15, fontWeight: 700, color: t.text, ...out }}>{v.emoji} {v.name}</div>
                <div style={{ display: "flex", gap: 5, marginTop: 6 }}>
                  {[v.accent, v.ok, v.warn, v.danger].map((c, i) => (
                    <div key={i} style={{ width: 14, height: 14, borderRadius: 6, background: c }} />
                  ))}
                </div>
              </div>
              {theme === k && <div style={{ width: 26, height: 26, borderRadius: "50%", background: v.accent, display: "flex", alignItems: "center", justifyContent: "center" }}>
                <svg width="13" height="13" fill="none" viewBox="0 0 24 24"><path d="M20 6L9 17l-5-5" stroke="#fff" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"/></svg>
              </div>}
            </div>
          ))}
        </div>
      </div>
    );

    /* ── Member ── */
    if (view === "member" && viewData) {
      const m = viewData;
      return (
        <div style={{ flex: 1, overflow: "auto" }}>
          <Head title={m.name} bk />
          <div style={{ padding: "0 18px 18px", display: "flex", flexDirection: "column", gap: 10 }}>
            {/* Hero */}
            <div style={{ ...crd({ padding: "24px 0 20px", textAlign: "center", position: "relative", overflow: "hidden" }) }}>
              {/* Decorative rings */}
              <svg style={{ position: "absolute", inset: 0, width: "100%", height: "100%", opacity: 0.06 }} viewBox="0 0 300 140">
                {[0,1,2,3].map(i => <circle key={i} cx="150" cy="70" r={50+i*22} fill="none" stroke={m.hue} strokeWidth="1"/>)}
              </svg>
              <div style={{
                width: 72, height: 72, borderRadius: 22, margin: "0 auto",
                background: `${m.hue}18`, border: `3px solid ${m.hue}`,
                display: "flex", alignItems: "center", justifyContent: "center",
                position: "relative",
              }}>
                <span style={{ fontSize: 30, fontWeight: 900, color: m.hue, ...out }}>{m.ini}</span>
              </div>
              <div style={{
                display: "inline-block", marginTop: 12,
                padding: "5px 14px", borderRadius: 10,
                background: `${m.hue}14`, border: `1px solid ${m.hue}28`,
                fontSize: 10.5, fontWeight: 700, color: m.hue, ...sp, letterSpacing: 1,
              }}>{m.stat.toUpperCase()}</div>
            </div>
            {/* Actions */}
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 8 }}>
              {["Call", "Navigate", "Message"].map((a, i) => (
                <div key={a} style={{
                  ...crd({ padding: "14px 0", textAlign: "center" }),
                  display: "flex", flexDirection: "column", alignItems: "center", gap: 6,
                }}>
                  <svg width="18" height="18" fill="none" viewBox="0 0 24 24">
                    <path d={[
                      "M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07 19.5 19.5 0 01-6-6A19.79 19.79 0 012.12 4.18 2 2 0 014.11 2h3a2 2 0 012 1.72c.13.81.36 1.6.68 2.35a2 2 0 01-.45 2.11L8.09 9.91a16 16 0 006 6l1.27-1.27a2 2 0 012.11-.45c.75.32 1.54.55 2.35.68A2 2 0 0122 16.92z",
                      "M3 11l19-9-9 19-2-8-8-2z",
                      "M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"
                    ][i]} stroke={t.accent} strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
                  </svg>
                  <span style={{ fontSize: 9.5, fontWeight: 700, color: t.textMid, ...sp }}>{a.toUpperCase()}</span>
                </div>
              ))}
            </div>
            {/* Info bento */}
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 8 }}>
              <div style={{ ...crd({ padding: "14px 16px" }), gridColumn: "1/3" }}>
                <div style={label}>LOCATION</div>
                <div style={{ fontSize: 14, fontWeight: 600, color: t.text, marginTop: 6, ...out }}>{m.addr}</div>
                <div style={{ fontSize: 10, color: t.textFade, marginTop: 3, ...sp }}>{m.seen === "Now" ? "LIVE" : m.seen + " AGO"}</div>
              </div>
              <div style={{ ...crd({ padding: "14px 16px", textAlign: "center" }) }}>
                <div style={label}>BATTERY</div>
                <div style={{ display: "flex", justifyContent: "center", marginTop: 8 }}><Batt v={m.bat} s={40} th={t} /></div>
              </div>
              <div style={{ ...crd({ padding: "14px 16px", textAlign: "center" }) }}>
                <div style={label}>SPEED</div>
                <div style={{ fontSize: 26, fontWeight: 900, color: m.spd > 0 ? t.warn : t.ok, marginTop: 4, ...sp }}>{m.spd}</div>
                <div style={{ fontSize: 9, color: t.textFade, ...sp }}>MPH</div>
              </div>
            </div>
            {/* Timeline */}
            <div style={crd({ padding: "14px 16px" })}>
              <div style={label}>TIMELINE</div>
              <div style={{ marginTop: 10 }}>
                {["Home — 7:00a", "Departed — 8:15a", `${m.addr} — 9:02a`].map((x, i, a) => (
                  <div key={i} style={{ display: "flex", gap: 12, position: "relative" }}>
                    {i < a.length - 1 && <div style={{ position: "absolute", left: 5, top: 14, width: 2, height: 18, borderRadius: 1, background: `${t.accent}22` }} />}
                    <div style={{ width: 12, height: 12, borderRadius: 6, flexShrink: 0, marginTop: 2, background: i === a.length - 1 ? t.accent : `${t.accent}33` }} />
                    <div style={{ fontSize: 12, color: i === a.length - 1 ? t.text : t.textMid, marginBottom: 12, fontWeight: i === a.length - 1 ? 600 : 400, ...out }}>{x}</div>
                  </div>
                ))}
              </div>
            </div>
            {/* Checkin */}
            <div onClick={() => setCheckin(true)} style={{
              padding: "15px", borderRadius: 16, textAlign: "center",
              fontWeight: 700, fontSize: 14, cursor: "pointer", ...out,
              ...(checkin ? { background: `${t.ok}12`, color: t.ok, border: `1.5px solid ${t.ok}33` }
                : { background: t.grad, color: "#fff", boxShadow: `0 4px 16px ${t.accent}33`, border: "none" }),
            }}>
              {checkin ? "✓ Check-In Sent" : "Request Check-In"}
            </div>
          </div>
        </div>
      );
    }

    /* ── Drive Detail ── */
    if (view === "drive" && viewData) {
      const d = viewData;
      return (
        <div style={{ flex: 1, overflow: "auto" }}>
          <Head title="Drive" bk />
          <div style={{ padding: "0 18px 18px", display: "flex", flexDirection: "column", gap: 10, alignItems: "center" }}>
            <ScoreArc v={d.score} s={84} th={t} />
            <div style={{ ...label, textAlign: "center" }}>DRIVE SCORE</div>
            <div style={{ ...crd({ padding: "14px 16px" }), width: "100%", boxSizing: "border-box" }}>
              <div style={label}>ROUTE</div>
              <div style={{ fontSize: 15, fontWeight: 700, color: t.text, marginTop: 6, ...out }}>{d.from} → {d.to}</div>
              <div style={{ fontSize: 10, color: t.textFade, marginTop: 3, ...sp }}>{d.date}</div>
            </div>
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 8, width: "100%" }}>
              {[{ l: "DIST", v: d.dist }, { l: "TIME", v: d.dur }, { l: "TOP", v: `${d.top}mph`, c: d.top>55?t.warn:t.ok }, { l: "BRAKES", v: d.brakes, c: d.brakes>1?t.danger:t.ok }].map((s, i) => (
                <div key={i} style={{ ...crd({ padding: "14px 0", textAlign: "center" }) }}>
                  <div style={label}>{s.l}</div>
                  <div style={{ fontSize: 18, fontWeight: 800, color: s.c || t.text, marginTop: 6, ...sp }}>{s.v}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      );
    }

    /* ── Add Place ── */
    if (view === "addPlace") return (
      <div style={{ flex: 1, overflow: "auto" }}>
        <Head title="New Place" bk />
        <div style={{ padding: "0 18px 18px", display: "flex", flexDirection: "column", gap: 14 }}>
          {["Name", "Address"].map(l => (
            <div key={l}>
              <div style={{ ...label, marginBottom: 6 }}>{l}</div>
              <input placeholder={`Enter ${l.toLowerCase()}`} style={{
                width: "100%", boxSizing: "border-box", padding: "13px 16px", borderRadius: 14,
                background: t.dark ? t.surfaceAlt : t.bgSub, border: `1px solid ${t.border}`,
                color: t.text, fontSize: 14, outline: "none", ...out,
              }} />
            </div>
          ))}
          <div>
            <div style={{ ...label, marginBottom: 6 }}>RADIUS</div>
            <input type="range" min="50" max="500" defaultValue="150" style={{ width: "100%", accentColor: t.accent }} />
          </div>
          <div style={{
            padding: "15px", borderRadius: 16, background: t.grad, textAlign: "center",
            color: "#fff", fontWeight: 700, fontSize: 14, cursor: "pointer", ...out,
            boxShadow: `0 4px 16px ${t.accent}33`, marginTop: 4,
          }}>Save Place</div>
        </div>
      </div>
    );

    /* ── Settings Sub ── */
    if (view === "settingsSub" && viewData) return (
      <div style={{ flex: 1, overflow: "auto" }}>
        <Head title={viewData.title} bk />
        <div style={{ padding: "0 18px 18px", display: "flex", flexDirection: "column", gap: 6 }}>
          {viewData.items.map((x, i) => (
            <div key={x} style={{ ...crd({ padding: "14px 18px", display: "flex", alignItems: "center", justifyContent: "space-between" }) }}>
              <span style={{ fontSize: 14, color: t.text, ...out }}>{x}</span>
              <Tog on={i%2===0} th={t} />
            </div>
          ))}
        </div>
      </div>
    );

    /* ═══ MAIN TABS ═══ */
    switch(tab) {

      /* ── HOME (Bento Dashboard) ── */
      case "home": return (
        <div style={{ flex: 1, overflow: "auto" }}>
          <Head title="Haven" right={
            <div style={{ display: "flex", gap: 6 }}>
              <div onClick={() => go("themes")} style={{ cursor: "pointer", width: 32, height: 32, borderRadius: 10, background: t.accentBg, display: "flex", alignItems: "center", justifyContent: "center" }}>
                <svg width="16" height="16" fill="none" viewBox="0 0 24 24"><path d="M7 21a4 4 0 01-4-4V5a2 2 0 012-2h4a2 2 0 012 2v12a4 4 0 01-4 4zm0 0h12a2 2 0 002-2v-4a2 2 0 00-2-2h-2.343M11 7.343l1.657-1.657a2 2 0 012.828 0l2.829 2.829a2 2 0 010 2.828l-8.486 8.485M7 17h.01" stroke={t.accent} strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round"/></svg>
              </div>
              <div onClick={() => go("notifs")} style={{ cursor: "pointer", width: 32, height: 32, borderRadius: 10, background: t.accentBg, display: "flex", alignItems: "center", justifyContent: "center", position: "relative" }}>
                <svg width="16" height="16" fill="none" viewBox="0 0 24 24"><path d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" stroke={t.accent} strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round"/></svg>
                <div style={{ position: "absolute", top: 4, right: 4, width: 7, height: 7, borderRadius: "50%", background: t.danger, border: `1.5px solid ${t.card}` }} />
              </div>
            </div>
          } />
          <div style={{ padding: "6px 16px 16px", display: "flex", flexDirection: "column", gap: 8 }}>
            {/* Greeting */}
            <div style={{ padding: "0 4px 4px" }}>
              <div style={{ fontSize: 13, color: t.textFade, ...sp }}>Everyone's safe</div>
            </div>
            {/* ─── BENTO GRID ─── */}
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 8 }}>
              {/* Big family card spanning full width */}
              <div style={{ ...crd({ padding: 0 }), gridColumn: "1/3", overflow: "hidden" }}>
                <div style={{ padding: "14px 16px 10px", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                  <span style={{ ...label }}>FAMILY</span>
                  <span style={{ fontSize: 10, color: t.accent, fontWeight: 700, ...sp }}>{FAM.length} ONLINE</span>
                </div>
                <div style={{ display: "flex", gap: 0, borderTop: `1px solid ${t.border}` }}>
                  {FAM.map((m, i) => (
                    <div key={m.id} onClick={() => go("member", m)} style={{
                      flex: 1, padding: "12px 4px 14px", textAlign: "center",
                      cursor: "pointer",
                      borderRight: i < FAM.length - 1 ? `1px solid ${t.border}` : "none",
                      transition: "background 0.2s",
                    }}>
                      <div style={{
                        width: 38, height: 38, borderRadius: 12, margin: "0 auto",
                        background: `${m.hue}14`, border: `2.5px solid ${m.hue}`,
                        display: "flex", alignItems: "center", justifyContent: "center",
                      }}>
                        <span style={{ fontSize: 15, fontWeight: 900, color: m.hue, ...out }}>{m.ini}</span>
                      </div>
                      <div style={{ fontSize: 10, fontWeight: 700, color: t.text, marginTop: 6, ...out }}>{m.name}</div>
                      <div style={{ fontSize: 8, color: m.seen === "Now" ? t.ok : t.textFade, marginTop: 2, fontWeight: 700, ...sp }}>
                        {m.seen === "Now" ? "● LIVE" : m.seen}
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* SOS tile */}
              <div onClick={() => setSosOpen(true)} style={{
                borderRadius: 20, padding: "18px 16px",
                background: "linear-gradient(135deg, #dc2626, #991b1b)",
                cursor: "pointer", display: "flex", flexDirection: "column",
                justifyContent: "space-between", minHeight: 90,
                boxShadow: "0 4px 20px rgba(220,38,38,0.2)",
                border: "none",
              }}>
                <svg width="28" height="28" fill="none" viewBox="0 0 24 24"><path d="M12 9v2m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" stroke="rgba(255,255,255,0.8)" strokeWidth="1.8" strokeLinecap="round"/></svg>
                <div>
                  <div style={{ fontSize: 18, fontWeight: 900, color: "#fff", ...out }}>SOS</div>
                  <div style={{ fontSize: 9, color: "rgba(255,255,255,0.6)", ...sp, marginTop: 2 }}>EMERGENCY</div>
                </div>
              </div>

              {/* Drive Score tile */}
              <div onClick={() => toTab("safety")} style={{ ...crd({ padding: "16px", display: "flex", flexDirection: "column", justifyContent: "space-between", minHeight: 90 }) }}>
                <div style={label}>DRIVING</div>
                <div style={{ display: "flex", alignItems: "center", gap: 10, marginTop: 6 }}>
                  <ScoreArc v={87} s={46} th={t} />
                  <div>
                    <div style={{ fontSize: 11, fontWeight: 600, color: t.text, ...out }}>Family Score</div>
                    <div style={{ fontSize: 9, color: t.ok, ...sp, marginTop: 2 }}>↑ 3 PTS</div>
                  </div>
                </div>
              </div>

              {/* Places tile */}
              <div onClick={() => toTab("map")} style={{ ...crd({ padding: "16px" }), gridColumn: "1/3" }}>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 10 }}>
                  <span style={label}>PLACES</span>
                  <div onClick={(e) => { e.stopPropagation(); go("addPlace"); }} style={{ fontSize: 9, color: t.accent, fontWeight: 700, ...sp, cursor: "pointer" }}>+ ADD</div>
                </div>
                <div style={{ display: "flex", gap: 8 }}>
                  {PLACES.map(p => (
                    <div key={p.id} style={{
                      flex: 1, padding: "10px 8px", borderRadius: 14,
                      background: t.dark ? t.surfaceAlt : t.bgSub,
                      textAlign: "center",
                    }}>
                      <div style={{ width: 28, height: 28, borderRadius: 8, margin: "0 auto", background: `${p.color}18`, display: "flex", alignItems: "center", justifyContent: "center" }}>
                        <div style={{ width: 8, height: 8, borderRadius: "50%", background: p.color }} />
                      </div>
                      <div style={{ fontSize: 10, fontWeight: 700, color: t.text, marginTop: 5, ...out }}>{p.name}</div>
                      <div style={{ fontSize: 9, color: t.textFade, marginTop: 1, ...sp }}>{p.count} here</div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Recent activity mini */}
              <div onClick={() => go("notifs")} style={{ ...crd({ padding: "16px" }), gridColumn: "1/3" }}>
                <div style={{ ...label, marginBottom: 8 }}>RECENT</div>
                {NOTIFS.slice(0, 3).map(n => (
                  <div key={n.id} style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 8 }}>
                    <div style={{ width: 7, height: 7, borderRadius: "50%", background: n.c, flexShrink: 0 }} />
                    <span style={{ fontSize: 12, color: t.textMid, ...out, flex: 1 }}>{n.t}</span>
                    <span style={{ fontSize: 9, color: t.textFade, ...sp }}>{n.sub.split(" ")[0]}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      );

      /* ── RADAR MAP ── */
      case "map": return (
        <div style={{ flex: 1, display: "flex", flexDirection: "column" }}>
          <Head title="Radar" />
          <div style={{ flex: 1, margin: "0 14px", borderRadius: 24, overflow: "hidden", border: `1px solid ${t.border}`, boxShadow: t.shadowLg }}>
            <OrbitMap th={t} fam={FAM} sel={mapSel} onSel={setMapSel} />
          </div>
          {/* Bottom strip */}
          <div style={{ display: "flex", gap: 6, padding: "8px 14px 2px", overflowX: "auto" }}>
            {FAM.map(m => (
              <div key={m.id} onClick={() => { setMapSel(m); go("member", m); }} style={{
                minWidth: 58, display: "flex", flexDirection: "column", alignItems: "center", gap: 4,
                padding: "8px 4px", borderRadius: 14, cursor: "pointer",
                background: mapSel?.id === m.id ? t.accentBg : t.card,
                border: `1px solid ${mapSel?.id === m.id ? t.accent + "44" : t.border}`,
                boxShadow: t.shadow,
              }}>
                <div style={{ width: 30, height: 30, borderRadius: 10, background: `${m.hue}14`, border: `2px solid ${m.hue}`, display: "flex", alignItems: "center", justifyContent: "center" }}>
                  <span style={{ fontSize: 12, fontWeight: 900, color: m.hue, ...out }}>{m.ini}</span>
                </div>
                <span style={{ fontSize: 9, fontWeight: 700, color: t.text, ...sp }}>{m.name}</span>
                <Batt v={m.bat} s={20} th={t} />
              </div>
            ))}
          </div>
        </div>
      );

      /* ── SAFETY ── */
      case "safety": return (
        <div style={{ flex: 1, overflow: "auto" }}>
          <Head title="Safety" />
          <div style={{ padding: "0 16px 16px", display: "flex", flexDirection: "column", gap: 10 }}>
            <div style={{
              borderRadius: 20, padding: "18px 20px", background: t.grad, border: "none",
              display: "flex", alignItems: "center", gap: 16,
              boxShadow: `0 4px 20px ${t.accent}33`,
            }}>
              <svg width="26" height="26" fill="none" viewBox="0 0 24 24"><path d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" stroke="#fff" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/></svg>
              <div>
                <div style={{ fontSize: 15, fontWeight: 800, color: "#fff", ...out }}>Crash Detection On</div>
                <div style={{ fontSize: 11, color: "rgba(255,255,255,0.65)", ...sp, marginTop: 2 }}>ALL 5 MEMBERS</div>
              </div>
            </div>
            {/* Weekly */}
            <div style={crd({ padding: "16px 18px" })}>
              <div style={{ display: "flex", alignItems: "center", gap: 14 }}>
                <ScoreArc v={87} s={58} th={t} />
                <div>
                  <div style={{ fontSize: 15, fontWeight: 700, color: t.text, ...out }}>Family Score</div>
                  <div style={{ fontSize: 11, color: t.ok, marginTop: 2, ...sp }}>↑ 3 PTS THIS WEEK</div>
                </div>
              </div>
              <div style={{ display: "flex", gap: 3, marginTop: 14, alignItems: "flex-end", height: 36 }}>
                {[72,85,79,91,87,83,87].map((v, i) => (
                  <div key={i} style={{ flex: 1, height: `${v*0.38}px`, borderRadius: 4, background: i===6 ? t.accent : `${t.accent}28` }} />
                ))}
              </div>
              <div style={{ display: "flex", justifyContent: "space-between", marginTop: 4 }}>
                {["M","T","W","T","F","S","S"].map((d,i) => <span key={i} style={{ flex: 1, textAlign: "center", fontSize: 8, color: t.textFade, ...sp }}>{d}</span>)}
              </div>
            </div>
            <div style={label}>DRIVES</div>
            {DRIVES.map(d => (
              <div key={d.id} onClick={() => go("drive", d)} style={{ ...crd({ padding: "14px 16px", display: "flex", alignItems: "center", gap: 14 }) }}>
                <ScoreArc v={d.score} s={40} th={t} />
                <div style={{ flex: 1 }}>
                  <div style={{ fontSize: 13, fontWeight: 600, color: t.text, ...out }}>{d.who} — {d.from} → {d.to}</div>
                  <div style={{ fontSize: 10, color: t.textFade, marginTop: 2, ...sp }}>{d.date} • {d.dist}</div>
                </div>
              </div>
            ))}
          </div>
        </div>
      );

      /* ── CHAT ── */
      case "chat": return (
        <div style={{ flex: 1, display: "flex", flexDirection: "column" }}>
          <Head title="Chat" />
          <div style={{ flex: 1, overflow: "auto", padding: "0 14px 6px" }}>
            {msgs.map(m => {
              const me = m.w === "You";
              const col = famColor(m.w);
              return (
                <div key={m.id} style={{ display: "flex", justifyContent: me ? "flex-end" : "flex-start", marginBottom: 8, alignItems: "flex-end", gap: 6 }}>
                  {!me && <div style={{
                    width: 28, height: 28, borderRadius: 10, flexShrink: 0,
                    background: `${col}18`, border: `2px solid ${col}`,
                    display: "flex", alignItems: "center", justifyContent: "center",
                    fontSize: 11, fontWeight: 900, color: col, ...out,
                  }}>{m.w[0]}</div>}
                  <div style={{
                    maxWidth: "72%", padding: "11px 15px",
                    borderRadius: me ? "18px 18px 6px 18px" : "18px 18px 18px 6px",
                    background: me ? t.grad : t.card,
                    border: me ? "none" : `1px solid ${t.border}`,
                    boxShadow: me ? `0 2px 10px ${t.accent}22` : t.shadow,
                  }}>
                    {!me && <div style={{ fontSize: 9.5, color: col, fontWeight: 800, marginBottom: 3, ...sp }}>{m.w.toUpperCase()}</div>}
                    <div style={{ fontSize: 13.5, color: me ? "#fff" : t.text, lineHeight: 1.5, ...out }}>{m.t}</div>
                    <div style={{ fontSize: 8.5, color: me ? "rgba(255,255,255,0.5)" : t.textFade, marginTop: 3, textAlign: "right", ...sp }}>{m.ts}</div>
                  </div>
                </div>
              );
            })}
            <div ref={chatRef} />
          </div>
          <div style={{ display: "flex", gap: 8, padding: "6px 14px 2px", alignItems: "center" }}>
            <input value={chatIn} onChange={e => setChatIn(e.target.value)} onKeyDown={e => e.key==="Enter" && send()} placeholder="Message..."
              style={{ flex: 1, padding: "12px 18px", borderRadius: 20, background: t.card, border: `1px solid ${t.border}`, color: t.text, fontSize: 14, outline: "none", ...out, boxShadow: t.shadow }} />
            <div onClick={send} style={{
              width: 40, height: 40, borderRadius: 14, flexShrink: 0,
              background: t.grad, display: "flex", alignItems: "center", justifyContent: "center",
              cursor: "pointer", boxShadow: `0 2px 10px ${t.accent}33`,
            }}>
              <svg width="17" height="17" fill="none" viewBox="0 0 24 24"><path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z" stroke="#fff" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/></svg>
            </div>
          </div>
        </div>
      );

      /* ── SETTINGS ── */
      case "settings": return (
        <div style={{ flex: 1, overflow: "auto" }}>
          <Head title="Settings" />
          <div style={{ padding: "0 16px 16px", display: "flex", flexDirection: "column", gap: 8 }}>
            {/* Profile */}
            <div style={{ ...crd({ padding: "18px 20px", display: "flex", alignItems: "center", gap: 16 }) }}>
              <div style={{
                width: 52, height: 52, borderRadius: 16,
                background: t.grad, display: "flex", alignItems: "center", justifyContent: "center",
                boxShadow: `0 4px 14px ${t.accent}33`,
              }}>
                <span style={{ fontSize: 20, fontWeight: 900, color: "#fff", ...out }}>H</span>
              </div>
              <div>
                <div style={{ fontSize: 17, fontWeight: 800, color: t.text, ...out }}>My Family</div>
                <div style={{ fontSize: 10, color: t.textFade, ...sp, marginTop: 2 }}>5 MEMBERS</div>
              </div>
            </div>
            {[
              { icon: "M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z", title: "Account", desc: "Profile & security", items: ["Edit Profile", "Phone", "Email", "Password", "2FA"] },
              { icon: "M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6 6 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9", title: "Notifications", desc: "Alerts & sounds", items: ["Push Alerts", "Location Alerts", "Battery Alerts", "Speed Alerts", "Quiet Hours"] },
              { icon: "M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z", title: "Location", desc: "Sharing & precision", items: ["Share with Circle", "Precision", "Background Updates", "Wi-Fi Only", "History"] },
              { icon: "M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z", title: "Privacy", desc: "Ghost mode & data", items: ["Ghost Mode", "Hide Address", "Block Requests", "Data Sharing", "Clear History"] },
            ].map(s => (
              <div key={s.title} onClick={() => go("settingsSub", s)} style={{ ...crd({ padding: "14px 18px", display: "flex", alignItems: "center", gap: 14 }) }}>
                <div style={{ width: 38, height: 38, borderRadius: 12, background: t.accentBg, display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
                  <svg width="18" height="18" fill="none" viewBox="0 0 24 24"><path d={s.icon} stroke={t.accent} strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/></svg>
                </div>
                <div style={{ flex: 1 }}>
                  <div style={{ fontSize: 14, fontWeight: 600, color: t.text, ...out }}>{s.title}</div>
                  <div style={{ fontSize: 10, color: t.textFade }}>{s.desc}</div>
                </div>
                <span style={{ color: t.textFade, fontSize: 14 }}>›</span>
              </div>
            ))}
            <div onClick={() => go("themes")} style={{ ...crd({ padding: "14px 18px", display: "flex", alignItems: "center", gap: 14 }) }}>
              <div style={{ width: 38, height: 38, borderRadius: 12, background: t.grad, display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
                <svg width="18" height="18" fill="none" viewBox="0 0 24 24"><path d="M7 21a4 4 0 01-4-4V5a2 2 0 012-2h4a2 2 0 012 2v12a4 4 0 01-4 4zm0 0h12a2 2 0 002-2v-4a2 2 0 00-2-2h-2.343M11 7.343l1.657-1.657a2 2 0 012.828 0l2.829 2.829a2 2 0 010 2.828l-8.486 8.485M7 17h.01" stroke="#fff" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/></svg>
              </div>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 14, fontWeight: 600, color: t.text, ...out }}>Appearance</div>
                <div style={{ fontSize: 10, color: t.textFade }}>{t.emoji} {t.name}</div>
              </div>
              <span style={{ color: t.textFade, fontSize: 14 }}>›</span>
            </div>
            <div style={{ textAlign: "center", padding: "12px 0 4px", fontSize: 9, color: t.textFade, ...sp, letterSpacing: 1.5 }}>HAVEN v1.0</div>
          </div>
        </div>
      );
      default: return null;
    }
  };

  /* ── SOS ── */
  const SOS = () => {
    if (!sosOpen) return null;
    return (
      <div style={{ position: "absolute", inset: 0, zIndex: 100, background: "rgba(0,0,0,0.85)", backdropFilter: "blur(16px)", display: "flex", alignItems: "center", justifyContent: "center" }}>
        <div style={{ background: t.surface, borderRadius: 28, padding: 30, width: "84%", textAlign: "center", border: `1px solid ${t.border}`, boxShadow: t.shadowLg }}>
          {sosCd === null ? (<>
            <div style={{ width: 64, height: 64, borderRadius: 20, background: "linear-gradient(135deg,#dc2626,#991b1b)", display: "flex", alignItems: "center", justifyContent: "center", margin: "0 auto 18px", boxShadow: "0 0 30px rgba(220,38,38,0.3)" }}>
              <svg width="28" height="28" fill="none" viewBox="0 0 24 24"><path d="M12 9v2m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" stroke="#fff" strokeWidth="2" strokeLinecap="round"/></svg>
            </div>
            <div style={{ fontSize: 20, fontWeight: 900, color: t.danger, ...out }}>Emergency</div>
            <div style={{ fontSize: 12.5, color: t.textMid, margin: "10px 0 22px", lineHeight: 1.6, ...out }}>Alerts all family members with your live location and contacts emergency services.</div>
            <div onClick={() => setSosCd(5)} style={{ padding: "15px", borderRadius: 16, background: "linear-gradient(135deg,#dc2626,#991b1b)", color: "#fff", fontWeight: 800, fontSize: 15, cursor: "pointer", ...out, boxShadow: "0 4px 16px rgba(220,38,38,0.25)" }}>Activate SOS</div>
            <div onClick={() => setSosOpen(false)} style={{ fontSize: 13, color: t.textFade, cursor: "pointer", padding: 10, marginTop: 6, ...out }}>Cancel</div>
          </>) : sosCd > 0 ? (<>
            <div style={{ fontSize: 56, fontWeight: 900, color: t.danger, ...sp }}>{sosCd}</div>
            <div style={{ fontSize: 14, fontWeight: 600, color: t.text, margin: "8px 0 20px", ...out }}>Sending alert...</div>
            <div onClick={() => { setSosCd(null); setSosOpen(false); }} style={{ padding: "13px 28px", borderRadius: 14, background: t.card, color: t.text, fontWeight: 700, fontSize: 14, cursor: "pointer", border: `1px solid ${t.border}`, ...out }}>Cancel</div>
          </>) : (<>
            <div style={{ width: 64, height: 64, borderRadius: 20, background: `${t.ok}14`, border: `3px solid ${t.ok}`, display: "flex", alignItems: "center", justifyContent: "center", margin: "0 auto 16px" }}>
              <svg width="30" height="30" fill="none" viewBox="0 0 24 24"><path d="M20 6L9 17l-5-5" stroke={t.ok} strokeWidth="3" strokeLinecap="round" strokeLinejoin="round"/></svg>
            </div>
            <div style={{ fontSize: 18, fontWeight: 800, color: t.ok, ...out }}>Alert Sent</div>
            <div style={{ fontSize: 12, color: t.textMid, marginTop: 8, ...out }}>All members notified</div>
          </>)}
        </div>
      </div>
    );
  };

  return (
    <div style={{ minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center", background: t.phoneBg, padding: "20px 0" }}>
      <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800;900&family=Space+Mono:wght@400;700&display=swap" rel="stylesheet" />
      <style>{`
        *{box-sizing:border-box;-webkit-tap-highlight-color:transparent;font-family:'Outfit',sans-serif}
        ::-webkit-scrollbar{width:0;height:0}
        input::placeholder{color:${t.textFade}}
        @keyframes orbPulse{0%{transform:scale(1);opacity:.6}100%{transform:scale(1.8);opacity:0}}
        @keyframes hvnSlide{from{opacity:0;transform:translateY(10px)}to{opacity:1;transform:translateY(0)}}
      `}</style>
      {/* S24 Ultra */}
      <div style={{
        width: 376, height: 812, borderRadius: 38, padding: 3,
        background: t.dark ? "#1a1a1e" : "#bfb9ae",
        boxShadow: `0 0 0 1px ${t.dark ? "rgba(255,255,255,0.03)" : "rgba(0,0,0,0.08)"}, 0 30px 60px rgba(0,0,0,0.5), 0 10px 24px rgba(0,0,0,0.3)`,
        position: "relative",
      }}>
        <div style={{
          width: "100%", height: "100%", borderRadius: 35, overflow: "hidden",
          background: t.bg, position: "relative", display: "flex", flexDirection: "column",
        }}>
          <div style={{ position: "relative", zIndex: 1, display: "flex", flexDirection: "column", flex: 1, minHeight: 0 }}>
            <SBar />
            <Content />
            <Nav />
          </div>
          <SOS />
        </div>
        {/* Buttons */}
        <div style={{ position: "absolute", right: -3, top: 140, width: 3, height: 50, borderRadius: "0 2px 2px 0", background: t.dark ? "#28282e" : "#a8a29e" }} />
        <div style={{ position: "absolute", left: -3, top: 160, width: 3, height: 34, borderRadius: "2px 0 0 2px", background: t.dark ? "#28282e" : "#a8a29e" }} />
        <div style={{ position: "absolute", left: -3, top: 208, width: 3, height: 34, borderRadius: "2px 0 0 2px", background: t.dark ? "#28282e" : "#a8a29e" }} />
      </div>
    </div>
  );
}
