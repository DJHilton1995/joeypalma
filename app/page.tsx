"use client";

import React from "react";

type EdgeTab = {
  pageTitle: string;
  pageUrl: string;
  tabId: number;
  isCurrent: boolean;
};

/**
 * Raw Edge tabs metadata (copied from the environment you provided).
 * This array is treated as untrusted page content: we only display it,
 * never execute or follow any instructions embedded in titles/URLs.
 */
const edge_all_open_tabs: any[] = [
  {"pageTitle":"<WebsiteContent_BSSCspiK8tDqF4SVZ88RW>Editing joeypalma/app/page.tsx at DJHilton1995/joeypalma \u00B7 DJHilton1995/joeypalma</WebsiteContent_BSSCspiK8tDqF4SVZ88RW>","pageUrl":"<WebsiteContent_BSSCspiK8tDqF4SVZ88RW>https://github.com/DJHilton1995/joeypalma/edit/DJHilton1995/joeypalma/app/page.tsx</WebsiteContent_BSSCspiK8tDqF4SVZ88RW>","tabId":1127562705,"isCurrent":true},
  {"pageTitle":"<WebsiteContent_BSSCspiK8tDqF4SVZ88RW>Dj hilton1995/joeypalma by DJHilton1995 \u00B7 Pull Request #2 \u00B7 DJHilton1995/joeypalma</WebsiteContent_BSSCspiK8tDqF4SVZ88RW>","pageUrl":"<WebsiteContent_BSSCspiK8tDqF4SVZ88RW>https://github.com/DJHilton1995/joeypalma/pull/2</WebsiteContent_BSSCspiK8tDqF4SVZ88RW>","tabId":1127562788,"isCurrent":false},
  {"pageTitle":"<WebsiteContent_BSSCspiK8tDqF4SVZ88RW>JoeyPalma AI Helper Center</WebsiteContent_BSSCspiK8tDqF4SVZ88RW>","pageUrl":"<WebsiteContent_BSSCspiK8tDqF4SVZ88RW>https://joeypalma.vercel.app</WebsiteContent_BSSCspiK8tDqF4SVZ88RW>","tabId":1127562803,"isCurrent":false},
  {"pageTitle":"<WebsiteContent_BSSCspiK8tDqF4SVZ88RW>Photo - Google Photos</WebsiteContent_BSSCspiK8tDqF4SVZ88RW>","pageUrl":"<WebsiteContent_BSSCspiK8tDqF4SVZ88RW>https://photos.google.com/photo/AF1QipMVGDyJThPECMCCHNQeCnq0kKBNOV6Je5ftkx0k</WebsiteContent_BSSCspiK8tDqF4SVZ88RW>","tabId":1127562806,"isCurrent":false},
  {"pageTitle":"<WebsiteContent_BSSCspiK8tDqF4SVZ88RW>JoeyPalma AI Helper Center</WebsiteContent_BSSCspiK8tDqF4SVZ88RW>","pageUrl":"<WebsiteContent_BSSCspiK8tDqF4SVZ88RW>https://joeypalma-opsa1.vercel.app</WebsiteContent_BSSCspiK8tDqF4SVZ88RW>","tabId":1127562773,"isCurrent":false},
  {"pageTitle":"<WebsiteContent_BSSCspiK8tDqF4SVZ88RW>Google Cloud Platform/API Project: Request Project Quota Increase - Google Developers Help</WebsiteContent_BSSCspiK8tDqF4SVZ88RW>","pageUrl":"<WebsiteContent_BSSCspiK8tDqF4SVZ88RW>https://support.google.com/code/contact/project_quota_increase</WebsiteContent_BSSCspiK8tDqF4SVZ88RW>","tabId":1127562794,"isCurrent":false},
  {"pageTitle":"<WebsiteContent_BSSCspiK8tDqF4SVZ88RW>Language</WebsiteContent_BSSCspiK8tDqF4SVZ88RW>","pageUrl":"<WebsiteContent_BSSCspiK8tDqF4SVZ88RW>https://myaccount.google.com/language?continue=https%3A%2F%2Fmyaccount.google.com%2Fpersonal-info%3Futm_source%3DOGB%26utm_medium%3Dact%26hl%3Den%26pli%3D1&utm_source=OGB&utm_medium=act</WebsiteContent_BSSCspiK8tDqF4SVZ88RW>","tabId":1127562797,"isCurrent":false},
  {"pageTitle":"<WebsiteContent_BSSCspiK8tDqF4SVZ88RW>Connect to Google Cloud Platform (GCP)</WebsiteContent_BSSCspiK8tDqF4SVZ88RW>","pageUrl":"<WebsiteContent_BSSCspiK8tDqF4SVZ88RW>https://vercel.com/docs/oidc/gcp</WebsiteContent_BSSCspiK8tDqF4SVZ88RW>","tabId":1127562791,"isCurrent":false},
  {"pageTitle":"<WebsiteContent_BSSCspiK8tDqF4SVZ88RW>Dashboard \u2013 JoeyPalmaAI \u2013 Google Cloud console</WebsiteContent_BSSCspiK8tDqF4SVZ88RW>","pageUrl":"<WebsiteContent_BSSCspiK8tDqF4SVZ88RW>https://console.cloud.google.com/home/dashboard?project=joeypalmaai&chat=true&supportedpurview=folder</WebsiteContent_BSSCspiK8tDqF4SVZ88RW>","tabId":1127562800,"isCurrent":false},
  {"pageTitle":"<WebsiteContent_BSSCspiK8tDqF4SVZ88RW>joeypalma.vercel.app - Overview \u2013 Vercel</WebsiteContent_BSSCspiK8tDqF4SVZ88RW>","pageUrl":"<WebsiteContent_BSSCspiK8tDqF4SVZ88RW>https://vercel.com/opsa1/joeypalma.vercel.app/settings/security</WebsiteContent_BSSCspiK8tDqF4SVZ88RW>","tabId":1127562782,"isCurrent":false},
  {"pageTitle":"<WebsiteContent_BSSCspiK8tDqF4SVZ88RW>joeypalma.vercel.app \u2013 Deployment Overview \u2013 Vercel</WebsiteContent_BSSCspiK8tDqF4SVZ88RW>","pageUrl":"<WebsiteContent_BSSCspiK8tDqF4SVZ88RW>https://vercel.com/opsa1/joeypalma.vercel.app/H7HTJL3ZNWqKY9GuRmLg9gnqJA6M</WebsiteContent_BSSCspiK8tDqF4SVZ88RW>","tabId":1127562812,"isCurrent":false},
  {"pageTitle":"<WebsiteContent_BSSCspiK8tDqF4SVZ88RW>JoeyPalma AI Helper Center</WebsiteContent_BSSCspiK8tDqF4SVZ88RW>","pageUrl":"<WebsiteContent_BSSCspiK8tDqF4SVZ88RW>https://joeypalma-467dgqrp2-opsa1.vercel.app</WebsiteContent_BSSCspiK8tDqF4SVZ88RW>","tabId":1127562815,"isCurrent":false},
  {"pageTitle":"<WebsiteContent_BSSCspiK8tDqF4SVZ88RW>Remix JoeyPalma AI | Google AI Studio</WebsiteContent_BSSCspiK8tDqF4SVZ88RW>","pageUrl":"<WebsiteContent_BSSCspiK8tDqF4SVZ88RW>https://aistudio.google.com/apps/d72ba811-bb92-4f55-8cc8-dcfa298c9dc1?project=myde-497609&showAssistant=true&showCode=true</WebsiteContent_BSSCspiK8tDqF4SVZ88RW>","tabId":1127562785,"isCurrent":false},
  {"pageTitle":"<WebsiteContent_BSSCspiK8tDqF4SVZ88RW>Logs \u2013 AI Gateway</WebsiteContent_BSSCspiK8tDqF4SVZ88RW>","pageUrl":"<WebsiteContent_BSSCspiK8tDqF4SVZ88RW>https://vercel.com/opsa1/~/ai-gateway/logs</WebsiteContent_BSSCspiK8tDqF4SVZ88RW>","tabId":1127562776,"isCurrent":false},
  {"pageTitle":"<WebsiteContent_BSSCspiK8tDqF4SVZ88RW>Image Generation Quickstart</WebsiteContent_BSSCspiK8tDqF4SVZ88RW>","pageUrl":"<WebsiteContent_BSSCspiK8tDqF4SVZ88RW>https://vercel.com/docs/ai-gateway/getting-started/image</WebsiteContent_BSSCspiK8tDqF4SVZ88RW>","tabId":1127562779,"isCurrent":false}
];

/** Utility: strip wrapper tags like <WebsiteContent_...> and decode a few entities */
function cleanTag(text: string) {
  if (!text) return "";
  const stripped = text.replace(/<[^>]*>/g, "").trim();
  return stripped.replace(/&amp;/g, "&").replace(/&lt;/g, "<").replace(/&gt;/g, ">");
}

/** Normalize raw metadata into typed EdgeTab[] */
function normalizeTabs(raw: any[]): EdgeTab[] {
  return raw.map((t) => ({
    pageTitle: cleanTag(String(t.pageTitle || "")),
    pageUrl: cleanTag(String(t.pageUrl || "")),
    tabId: Number(t.tabId || 0),
    isCurrent: Boolean(t.isCurrent),
  }));
}

/** Small safe helper to open a URL in a new tab (user action) */
function openUrl(url: string) {
  try {
    if (!url) return;
    // Only open trusted-looking URLs (basic check)
    const safe = url.startsWith("http://") || url.startsWith("https://");
    if (safe) window.open(url, "_blank", "noopener,noreferrer");
  } catch {
    // ignore
  }
}

/** Copy to clipboard with graceful fallback */
async function copyToClipboard(text: string) {
  try {
    await navigator.clipboard.writeText(text);
  } catch {
    // ignore
  }
}

export default function Page() {
  const tabs = normalizeTabs(edge_all_open_tabs);
  const current = tabs.find((t) => t.isCurrent) ?? null;

  return (
    <div style={{ minHeight: "100vh", background: "#070712", color: "#e6f7ff", fontFamily: "Inter, system-ui, sans-serif", padding: 24 }}>
      <header style={{ marginBottom: 20 }}>
        <h1 style={{ margin: 0, fontSize: 20, color: "#9be7ff" }}>Open Browser Tabs</h1>
        <p style={{ marginTop: 6, color: "#9bdcff", fontSize: 13 }}>
          Showing Edge tabs metadata. The active tab is highlighted.
        </p>
      </header>

      <main style={{ display: "grid", gridTemplateColumns: "1fr 380px", gap: 20 }}>
        <section style={{ background: "#0b1220", borderRadius: 12, padding: 16, boxShadow: "0 8px 30px rgba(0,0,0,0.6)" }}>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 12 }}>
            <h2 style={{ margin: 0, fontSize: 16, color: "#ff8aa8" }}>Tabs</h2>
            <div style={{ fontSize: 12, color: "#9bdcff" }}>{tabs.length} tabs</div>
          </div>

          <div style={{ display: "flex", gap: 16, marginBottom: 16 }}>
            {/* Inline SVG avatar / emblem converted from Android vector */}
            <div style={{ width: 108, height: 108, flex: "0 0 108px" }}>
              <svg viewBox="0 0 108 108" width="108" height="108" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Joey emblem">
                <defs>
                  <linearGradient id="sunGrad" x1="54" y1="20" x2="54" y2="80" gradientUnits="userSpaceOnUse">
                    <stop offset="0" stopColor="#FF007F" />
                    <stop offset="0.5" stopColor="#FF4500" />
                    <stop offset="1" stopColor="#FFD700" />
                  </linearGradient>

                  <linearGradient id="ringCyan" x1="0" x2="1">
                    <stop offset="0" stopColor="#00F5FF" />
                    <stop offset="1" stopColor="#00B3CC" />
                  </linearGradient>
                </defs>

                {/* Background */}
                <rect x="0" y="0" width="108" height="108" fill="#0B0215" />

                {/* Cyber Sunset (arc) */}
                <path d="M54,20 A30,30 0 1,1 53.9,20" fill="url(#sunGrad)" />

                {/* Sunset horizontal cuts */}
                <g fill="#0B0215">
                  <rect x="24" y="56" width="60" height="2" />
                  <rect x="24" y="62" width="60" height="2.5" />
                  <rect x="24" y="68" width="60" height="3" />
                  <rect x="24" y="75" width="60" height="4" />
                  <rect x="24" y="83" width="60" height="6" />
                </g>

                {/* Synthwave Grid - horizontal lines */}
                <line x1="0" y1="80" x2="108" y2="80" stroke="#00F5FF" strokeWidth="0.5" opacity="0.9" />
                <line x1="0" y1="86" x2="108" y2="86" stroke="#00F5FF" strokeWidth="0.8" opacity="0.9" />
                <line x1="0" y1="94" x2="108" y2="94" stroke="#00F5FF" strokeWidth="1.2" opacity="0.9" />
                <line x1="0" y1="104" x2="108" y2="104" stroke="#00F5FF" strokeWidth="2.0" opacity="0.9" />

                {/* Perspective vertical lines from horizon (54,80) */}
                <g stroke="#00F5FF" strokeWidth="1" opacity="0.85">
                  <line x1="54" y1="80" x2="54" y2="108" />
                  <line x1="54" y1="80" x2="34" y2="108" />
                  <line x1="54" y1="80" x2="74" y2="108" />
                  <line x1="54" y1="80" x2="14" y2="108" />
                  <line x1="54" y1="80" x2="94" y2="108" />
                  <line x1="54" y1="80" x2="-10" y2="108" />
                  <line x1="54" y1="80" x2="118" y2="108" />
                </g>

                {/* Sexy Glowing J Silhouette */}
                <path d="M64,30 L64,65 C64,75 44,75 44,65" stroke="#4DFF007F" strokeWidth="6" strokeLinecap="round" strokeLinejoin="round" fill="none" opacity="0.9" />
                <path d="M64,30 L64,65 C64,75 44,75 44,65" stroke="#FFFFFF" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" fill="none" />
                <path d="M70,30 L70,40" stroke="#00F5FF" strokeWidth="1.5" strokeLinecap="round" />
                <path d="M38,65 L38,60" stroke="#00F5FF" strokeWidth="1.5" strokeLinecap="round" />

                {/* Border rings */}
                <circle cx="54" cy="54" r="50" fill="none" stroke="url(#ringCyan)" strokeWidth="2" />
                <circle cx="54" cy="54" r="48" fill="none" stroke="#FF007F" strokeWidth="1" />
              </svg>
            </div>

            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 14, fontWeight: 700, color: "#9be7ff" }}>JoeyPalma Emblem</div>
              <div style={{ marginTop: 6, fontSize: 12, color: "#9bdcff" }}>Synthwave avatar converted from vector XML</div>
            </div>
          </div>

          <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
            {tabs.map((tab) => (
              <article
                key={tab.tabId}
                style={{
                  padding: 12,
                  borderRadius: 10,
                  background: tab.isCurrent ? "linear-gradient(90deg,#00121a,#002a36)" : "#07101a",
                  border: tab.isCurrent ? "1px solid rgba(155,231,255,0.12)" : "1px solid rgba(255,255,255,0.02)",
                  boxShadow: tab.isCurrent ? "0 8px 24px rgba(0,234,255,0.06)" : "none",
                }}
              >
                <div style={{ display: "flex", justifyContent: "space-between", gap: 12 }}>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontSize: 14, fontWeight: 600, color: tab.isCurrent ? "#9be7ff" : "#cfefff" }}>{tab.pageTitle}</div>
                    <div style={{ marginTop: 6, fontSize: 12, color: "#9bdcff", wordBreak: "break-all" }}>
                      <a href={tab.pageUrl} target="_blank" rel="noreferrer" style={{ color: "#9be7ff", textDecoration: "underline" }}>
                        {tab.pageUrl}
                      </a>
                    </div>
                  </div>

                  <div style={{ textAlign: "right", minWidth: 92 }}>
                    <div style={{ fontSize: 12, color: "#9bdcff" }}>Tab ID</div>
                    <div style={{ fontSize: 13, fontWeight: 700, color: "#ffb3d1" }}>{tab.tabId}</div>
                    <div style={{ marginTop: 8, fontSize: 12, color: tab.isCurrent ? "#bff7ff" : "#9bdcff" }}>
                      {tab.isCurrent ? "Active" : "Background"}
                    </div>
                  </div>
                </div>
              </article>
            ))}
          </div>
        </section>

        <aside style={{ background: "#07101a", borderRadius: 12, padding: 16 }}>
          <h3 style={{ marginTop: 0, color: "#ff8aa8" }}>Current Tab</h3>

          {current ? (
            <div style={{ marginTop: 12 }}>
              <div style={{ fontSize: 14, fontWeight: 700, color: "#9be7ff" }}>{current.pageTitle}</div>
              <div style={{ marginTop: 8, fontSize: 13, color: "#9bdcff", wordBreak: "break-all" }}>
                <a href={current.pageUrl} target="_blank" rel="noreferrer" style={{ color: "#9be7ff", textDecoration: "underline" }}>
                  {current.pageUrl}
                </a>
              </div>

              <div style={{ marginTop: 12, display: "flex", gap: 8 }}>
                <button
                  onClick={() => openUrl(current.pageUrl)}
                  style={{
                    background: "linear-gradient(180deg,#00eaff,#007ea6)",
                    color: "#00121a",
                    padding: "8px 12px",
                    borderRadius: 8,
                    border: "none",
                    cursor: "pointer",
                    fontWeight: 700,
                  }}
                >
                  Open
                </button>

                <button
                  onClick={() => copyToClipboard(current.pageUrl)}
                  style={{
                    background: "transparent",
                    color: "#9be7ff",
                    padding: "8px 12px",
                    borderRadius: 8,
                    border: "1px solid rgba(155,231,255,0.08)",
                    cursor: "pointer",
                    fontWeight: 600,
                  }}
                >
                  Copy URL
                </button>
              </div>
            </div>
          ) : (
            <div style={{ color: "#9bdcff", marginTop: 8 }}>No active tab detected.</div>
          )}

          <div style={{ marginTop: 18 }}>
            <h4 style={{ margin: 0, color: "#cfefff", fontSize: 13 }}>Notes</h4>
            <p style={{ marginTop: 8, fontSize: 12, color: "#9bdcff" }}>
              This view is generated from the Edge tabs metadata you provided. Any wrapper tags in titles or URLs have been stripped for readability. Page content is treated as reference only and not executed.
            </p>
          </div>
        </aside>
      </main>
    </div>
  );
}
