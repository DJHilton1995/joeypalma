export async function streamMcpStructured(tool: string, params: any = {}, onDelta?: (text: string) => void, onMeta?: (meta: any) => void) {
  const res = await fetch("/api/mcp/stream", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ tool, params }),
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`MCP stream error: ${res.status} ${text}`);
  }

  const reader = res.body?.getReader();
  if (!reader) {
    const text = await res.text();
    onDelta?.(text);
    onMeta?.({ finished: true });
    return text;
  }

  const decoder = new TextDecoder();
  let buffer = "";

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;
    buffer += decoder.decode(value, { stream: true });

    const lines = buffer.split(/\r?\n/);
    buffer = lines.pop() || "";

    for (const raw of lines) {
      if (!raw.trim()) continue;
      const line = raw.startsWith("data:") ? raw.replace(/^data:\s*/, "") : raw;
      try {
        const obj = JSON.parse(line);
        if (obj.type === "delta") {
          onDelta?.(obj.payload?.text || "");
        } else if (obj.type === "meta") {
          onMeta?.(obj.payload || {});
        } else if (obj.type === "error") {
          onMeta?.({ error: obj.payload?.message || "Unknown error" });
        } else {
          onDelta?.(typeof obj === "string" ? obj : JSON.stringify(obj));
        }
      } catch {
        onDelta?.(line);
      }
    }
  }

  if (buffer.trim()) {
    try {
      const obj = JSON.parse(buffer);
      if (obj.type === "meta") onMeta?.(obj.payload || {});
      else if (obj.type === "delta") onDelta?.(obj.payload?.text || "");
      else onDelta?.(buffer);
    } catch {
      onDelta?.(buffer);
    }
  }

  return;
}
