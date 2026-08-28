"use client";

import { useState } from "react";
import { callMcp } from "./src/lib/mcpClient";
import NeonPanel from "./src/components/NeonPanel";
import CyberButton from "./src/components/CyberButton";

export default function Home() {
  const [output, setOutput] = useState("");

  async function handlePing() {
    const res = await callMcp("ping");
    setOutput(JSON.stringify(res, null, 2));
  }

  async function handleReadFile() {
    const res = await callMcp("read_file", { path: "test.txt" });
    setOutput(JSON.stringify(res, null, 2));
  }

  return (
    <main className="min-h-screen bg-[#02030a] text-white flex items-center justify-center px-4 py-6">
      <div className="max-w-6xl w-full grid grid-cols-1 md:grid-cols-[1.2fr_1fr] gap-6">

        {/* LEFT PANEL */}
        <section className="relative rounded-2xl border border-[#ff003c55] bg-gradient-to-br from-[#050816] via-[#090020] to-[#00101f] p-6 shadow-[0_0_40px_#ff003c55] overflow-hidden">

          {/* Glow Orbs */}
          <div className="absolute inset-0 opacity-20 pointer-events-none">
            <div className="absolute -left-10 top-10 w-40 h-40 rounded-full bg-[#ff003c] blur-3xl" />
            <div className="absolute right-0 bottom-0 w-52 h-52 rounded-full bg-[#00eaff] blur-3xl" />
          </div>

          {/* Avatar + Title */}
          <div className="relative flex flex-col md:flex-row items-center gap-6">
            <div className="flex flex-col items-center gap-3 glow-pulse">
              <div className="w-32 h-32 rounded-full border border-[#ff003c] bg-[#050816] flex items-center justify-center">
                <span className="text-xs tracking-[0.25em] text-[#ff8aa8] neon-text">
                  JOEY
                </span>
              </div>
              <div className="text-center">
                <p className="text-[0.7rem] tracking-[0.3em] text-[#ff8aa8] uppercase">
                  JOEY // NEON ARCADE
                </p>
                <p className="mt-1 text-xs text-[#9bdcff]">
                  Your personal AI helper center.
                </p>
              </div>
            </div>

            {/* MCP Tools */}
            <div className="flex-1 space-y-4">
              <NeonPanel title="MCP Tools">
                <div className="flex flex-col gap-3">
                  <CyberButton onClick={handlePing}>
                    🔮 Ping MCP Server
                  </CyberButton>

                  <CyberButton color="blue" onClick={handleReadFile}>
                    📁 Read File (test.txt)
                  </CyberButton>
                </div>
              </NeonPanel>
            </div>
          </div>
        </section>

        {/* RIGHT PANEL */}
        <NeonPanel title="MCP Output">
          <pre className="whitespace-pre-wrap text-sm bg-black/60 p-4 rounded-lg">
            {output || "No output yet."}
          </pre>
        </NeonPanel>

      </div>
    </main>
  );
}
