export async function callMcp(tool: string, params: any = {}) {
  const res = await fetch("http://127.0.0.1:8080/mcp", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": "Bearer super-secure-token"
    },
    body: JSON.stringify({
      id: crypto.randomUUID(),
      tool,
      params
    })
  });

  return await res.json();
}
