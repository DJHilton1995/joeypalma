"use client";

import { useState, useRef } from "react";
import { streamMcp } from "../lib/mcpStream";
import CyberButton from "./CyberButton";

type Message = { id: string; role: "user" | "assistant"; text: string };

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
    const userMsg: Message = { id: crypto.randomUUID(), role: "user", text: input.trim() };
    pushMessage(userMsg);
    setInput("");
    setLoading(true);

    const assistantId = crypto.randomUUID();
    pushMessage({ id: assistantId, role: "assistant", text: "" });

    try {
      await streamMcp("chat", { prompt: userMsg.text, history: messages }, (chunk) => {
        setMessages((prev) =>
          prev.map((m) => (m.id === assistantId ? { ...m, text: m.text + chunk } : m))
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

  return (
    <div className="neon-panel rounded-xl-2 p-4">
      <h3 className="text-sm font-semibold accent-cyan mb-3">Joey — Live Assistant</h3>

      <div ref={scrollRef} className="max-h-56 overflow-auto p-2 space-y-3 bg-black/20 rounded mb-3">
        {messages.length === 0 && <div className="text-xs text-[#9bdcff]">No messages yet. Ask Joey something.</div>}
        {messages.map((m) => (
          <div key={m.id} className={`p-2 rounded ${m.role === "user" ? "bg-black/30 text-[#ff8aa8]" : "bg-black/10 text-[#9bdcff]"}`}>
            <div className="text-xs font-medium">{m.role === "user" ? "You" : "Joey"}</div>
            <div className="text-sm whitespace-pre-wrap mt-1">{m.text}</div>
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
