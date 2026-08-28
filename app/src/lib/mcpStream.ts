// app/src/lib/mcpStream.ts
export async function streamMcp(tool: string, params: any = {}, onChunk?: (chunk: string) => void) {
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
    onChunk?.(text);
    return text;
  }

  const decoder = new TextDecoder();
  let accumulated = "";
  while (true) {
    const { done, value } = await reader.read();
    if (done) break;
    const chunk = decoder.decode(value, { stream: true });
    // handle SSE-style "data: ..." lines
    const lines = chunk.split(/\r?\n/);
    for (const line of lines) {
      if (!line) continue;
      if (line.startsWith("data:")) {
        const payload = line.replace(/^data:\s*/, "");
        onChunk?.(payload);
        accumulated += payload;
      } else {
        // raw chunk
        onChunk?.(line);
        accumulated += line;
      }
    }
  }
  return accumulated;
}
