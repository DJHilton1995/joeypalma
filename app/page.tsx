"use client";

import Image from "next/image";
import { useState } from "react";
import NeonPanel from "./src/components/NeonPanel";
import CyberButton from "./src/components/CyberButton";
import Fingerprint from "./src/components/Fingerprint";
import { callMcp } from "./src/lib/mcpClient";

export default function Home() {
  const [output, setOutput] = useState<string>("");
  const [loading, setLoading] = useState(false);

  async function handleGetAiHelp() {
    try {
      setLoading(true);
      setOutput("");
      const res = await callMcp("start_ai_help", { context: "homepage_request" });
      setOutput(JSON.stringify(res, null, 2));
    } catch (err: any) {
      setOutput(err.message || "Error calling MCP");
    } finally {
      setLoading(false);
    }
  }

  async function handlePing() {
    try {
      setLoading(true);
      setOutput("");
      const res = await callMcp("ping");
      setOutput(JSON.stringify(res, null, 2));
    } catch (err: any) {
      setOutput(err.message || "Error calling MCP");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="min-h-screen relative bg-[#02030a] text-white overflow-hidden scanlines">
      <div className="absolute inset-0 -z-10">
        <Image src="/hero.jpg" alt="Hero" fill className="object-cover opacity-90" priority />
        <div className="absolute inset-0 bg-gradient-to-b from-black/10 via-black/40 to-black/70" />
      </div>

      <div className="glow-orb" style={{ left: -120, top: 40, width: 360, height: 360, background: "radial-gradient(circle,#ff3b6b55,transparent 40%)" }} />
      <div className="glow-orb" style={{ right: -160, bottom: -40, width: 420, height: 420, background: "radial-gradient(circle,#00eaff55,transparent 40%)" }} />

      <div className="relative z-10 max-w-7xl mx-auto px-6 py-10 grid grid-cols-1 lg:grid-cols-12 gap-6">
        <div className="lg:col-span-5 space-y-6">
          <div className="neon-panel rounded-xl-2 p-6">
            <h1 className="text-2xl font-bold neon-text">MY AI HELPER CENTER</h1>
            <p className="mt-3 text-sm text-[#9bdcff]">JOEY // CYBER SECURITY AI</p>

            <div className="mt-6 flex items-center gap-4">
              <div className="w-28 h-28 rounded-full border border-[#ff3b6b33] overflow-hidden shadow-[0_0_30px_#ff3b6b33]">
                <Image src="/hero.jpg" alt="Joey avatar" width={112} height={112} className="object-cover" />
              </div>

              <div>
                <p className="text-sm text-[#ff8aa8] tracking-wider uppercase">JOEY // NEON ARCADE</p>
                <p className="text-xs text-[#9bdcff] mt-1">Your security assistant and system guardian.</p>
              </div>
            </div>
          </div>

          <NeonPanel title="Quick Actions">
            <div className="flex flex-col gap-3">
              <CyberButton onClick={handleGetAiHelp} color="cyan">
                {loading ? "Starting…" : "GET AI HELP"}
              </CyberButton>

              <div className="flex gap-3 mt-2">
                <CyberButton onClick={() => alert("Ready")}>READY</CyberButton>
                <CyberButton onClick={() => alert("Settings")} color="cyan">SETTINGS</CyberButton>
              </div>

              <div className="mt-3 flex items-center gap-3">
                <Fingerprint className="pulse" />
                <div className="text-xs text-[#9bdcff]">
                  <div className="font-semibold accent-cyan">MY SECURITY AI</div>
                  <div>Everything is safe and running well.</div>
                </div>
              </div>
            </div>
          </NeonPanel>
        </div>

        <div className="lg:col-span-4">
          <NeonPanel title="Live Status">
            <div className="flex flex-col gap-3">
              <div className="p-4 bg-black/30 rounded-lg">
                <p className="text-sm accent-cyan">SYSTEM STATUS</p>
                <p className="mt-2 text-xs text-[#9bdcff]">EVERYTHING IS SAFE AND RUNNING WELL.</p>
              </div>

              <div className="mt-3">
                <h4 className="text-sm font-medium accent-cyan">MCP Tools</h4>
                <div className="mt-2 flex gap-2">
                  <CyberButton onClick={handlePing}>Ping MCP</CyberButton>
                </div>
              </div>

              <div className="mt-4">
                <h4 className="text-sm font-medium accent-cyan">MCP Output</h4>
                <pre className="whitespace-pre-wrap text-xs bg-black/40 p-3 rounded mt-2 text-[#9bdcff] max-h-40 overflow-auto">
                  {output || "No output yet."}
                </pre>
              </div>
            </div>
          </NeonPanel>
        </div>

        <div className="lg:col-span-3">
          <NeonPanel title="PAST HELP LOG">
            <ul className="text-sm text-[#9bdcff] space-y-2">
              <li>Check on my security.</li>
              <li>Is my network stable?</li>
              <li>Block that bad file.</li>
              <li>Update my security walls.</li>
            </ul>
          </NeonPanel>
        </div>
      </div>

      <div className="relative z-10 max-w-7xl mx-auto px-6 pb-10">
        <div className="neon-panel rounded-xl-2 p-4 mt-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm accent-cyan font-semibold">MY SECURITY AI</p>
              <p className="text-xs text-[#9bdcff]">Everything is safe and running well.</p>
            </div>
            <div>
              <button className="cyber-btn cyber-btn-cyan" onClick={() => alert("View details")}>View Details</button>
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}
