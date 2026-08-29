export async function callMcp(tool: string, params: any = {}) {
  const res = await fetch("/api/mcp", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ tool, params }),
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(`MCP proxy error: ${res.status} ${text}`);
  }
  return res.json();
}
