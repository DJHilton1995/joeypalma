"use client";

import { useState, useRef } from "react";
import { streamMcpStructured } from "../lib/mcpStream";
import CyberButton from "./CyberButton";

type Message = { id: string; role: "user" | "assistant"; text: string; meta?: any };

export default function ChatPanel() {
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const scrollRef = useRef<HTMLDivElement | null>(null);

  function pushMessage(msg: Message) {
    setMessages((m) => [...m, msg]);
    setTimeout(() => scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: "smooth" }), 50);
  }

  async function send() {
    if (!input.trim()) return;
    const userText = input.trim();
    const userMsg: Message = { id: crypto.randomUUID(), role: "user", text: userText };
    pushMessage(userMsg);
    setInput("");
    setLoading(true);

    const assistantId = crypto.randomUUID();
    pushMessage({ id: assistantId, role: "assistant", text: "" });

    try {
      await streamMcpStructured("chat", { prompt: userText, history: messages }, (chunk) => {
        setMessages((prev) =>
          prev.map((m) => (m.id === assistantId ? { ...m, text: m.text + chunk } : m))
        );
      }, (meta) => {
        setMessages((prev) =>
          prev.map((m) => (m.id === assistantId ? { ...m, meta } : m))
        );
      });
    } catch (err: any) {
      setMessages((prev) =>
        prev.map((m) => (m.id === assistantId ? { ...m, text: `Error: ${err.message || "stream failed"}` } : m))
      );
    } finally {
      setLoading(false);
      setTimeout(() => scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: "smooth" }), 50);
    }
  }

  function renderMeta(meta: any) {
    if (!meta) return null;
    const scores = meta.scores || {};
    return (
      <div className="mt-2 text-xs text-[#9bdcff]">
        <div className="flex gap-2 flex-wrap">
          {["intelligence","complexity","accuracy","validity","humanity"].map((k) => (
            <div key={k} className="px-2 py-1 bg-black/30 rounded text-[11px]">
              <div className="font-semibold">{k}</div>
              <div>{scores[k] ?? "—"}/10</div>
            </div>
          ))}
        </div>

        {meta.reasoning && <div className="mt-2 italic text-[12px]">{meta.reasoning}</div>}

        {meta.sources?.length > 0 && (
          <ul className="mt-2 space-y-1">
            {meta.sources.map((s: string, i: number) => (
              <li key={i}><a href={s} target="_blank" rel="noreferrer" className="underline text-[#9be7ff]">{s}</a></li>
            ))}
          </ul>
        )}
      </div>
    );
  }

  return (
    <div className="neon-panel rounded-xl-2 p-4">
      <h3 className="text-sm font-semibold accent-cyan mb-3">Joey — Live Assistant</h3>

      <div ref={scrollRef} className="max-h-56 overflow-auto p-2 space-y-3 bg-black/20 rounded mb-3">
        {messages.length === 0 && <div className="text-xs text-[#9bdcff]">No messages yet. Ask Joey something.</div>}
        {messages.map((m) => (
          <div key={m.id} className={`p-2 rounded ${m.role === "user" ? "bg-black/30 text-[#ff8aa8]" : "bg-black/10 text-[#9bdcff]"}`}>
            <div className="text-xs font-medium">{m.role === "user" ? "You" : "Joey"}</div>
            <div className="text-sm whitespace-pre-wrap mt-1">{m.text}</div>
            {m.meta && renderMeta(m.meta)}
          </div>
        ))}
        {loading && <div className="text-xs text-[#9bdcff]">Joey is typing…</div>}
      </div>

      <div className="flex gap-2">
        <input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => { if (e.key === "Enter") send(); }}
          placeholder="Ask Joey about security, network, or system status"
          className="flex-1 p-2 rounded bg-black/20 text-sm outline-none"
        />
        <CyberButton onClick={send} color="cyan">{loading ? "…" : "Send"}</CyberButton>
      </div>
    </div>
  );
}
