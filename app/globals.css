"use client";

import Image from "next/image";
import NeonPanel from "./src/components/NeonPanel";
import CyberButton from "./src/components/CyberButton";

export default function Home() {
  return (
    <main className="min-h-screen relative bg-[#02030a] text-white overflow-hidden scanlines">
      {/* Background hero image */}
      <div className="absolute inset-0 -z-10">
        <Image src="/hero.jpg" alt="Hero" fill className="object-cover opacity-90" priority />
        <div className="absolute inset-0 bg-gradient-to-b from-black/10 via-black/40 to-black/70" />
      </div>

      {/* Decorative orbs */}
      <div className="glow-orb" style={{ left: -120, top: 40, width: 360, height: 360, background: "radial-gradient(circle,#ff3b6b55,transparent 40%)" }} />
      <div className="glow-orb" style={{ right: -160, bottom: -40, width: 420, height: 420, background: "radial-gradient(circle,#00eaff55,transparent 40%)" }} />

      <div className="relative z-10 max-w-7xl mx-auto px-6 py-10 grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left hero column */}
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
              <CyberButton onClick={() => alert("Start AI Help")} color="cyan">GET AI HELP</CyberButton>
              <div className="flex gap-3 mt-2">
                <CyberButton onClick={() => alert("Ready")} >READY</CyberButton>
                <CyberButton onClick={() => alert("Settings")} color="cyan">SETTINGS</CyberButton>
              </div>
            </div>
          </NeonPanel>
        </div>

        {/* Center column */}
        <div className="lg:col-span-4">
          <NeonPanel title="Live Status">
            <div className="flex flex-col gap-3">
              <div className="p-4 bg-black/30 rounded-lg">
                <p className="text-sm accent-cyan">SYSTEM STATUS</p>
                <p className="mt-2 text-xs text-[#9bdcff]">EVERYTHING IS SAFE AND RUNNING WELL.</p>
              </div>
              <div className="mt-3">
                <h4 className="text-sm font-medium accent-cyan">MY SECURITY AI</h4>
                <p className="text-xs text-[#9bdcff] mt-1">Monitoring network, files, and system integrity.</p>
              </div>
            </div>
          </NeonPanel>
        </div>

        {/* Right column */}
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

      {/* Footer small panel */}
      <div className="relative z-10 max-w-7xl mx-auto px-6 pb-10">
        <div className="neon-panel rounded-xl-2 p-4 mt-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm accent-cyan font-semibold">MY SECURITY AI</p>
              <p className="text-xs text-[#9bdcff]">Everything is safe and running well.</p>
            </div>
            <div>
              <button className="cyber-btn-cyan cyber-btn">View Details</button>
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}
