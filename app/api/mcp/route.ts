import { NextResponse } from "next/server";

export async function POST(req: Request) {
  try {
    const body = await req.json();
    const MCP_URL = process.env.MCP_URL;
    const MCP_TOKEN = process.env.MCP_TOKEN;

    if (!MCP_URL || !MCP_TOKEN) {
      return NextResponse.json({ error: "MCP_URL or MCP_TOKEN not configured" }, { status: 500 });
    }

    const resp = await fetch(MCP_URL, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${MCP_TOKEN}`,
      },
      body: JSON.stringify({
        id: crypto.randomUUID(),
        tool: body.tool,
        params: body.params || {},
      }),
    });

    const data = await resp.text();
    const contentType = resp.headers.get("content-type") || "application/json";
    return new NextResponse(data, { status: resp.status, headers: { "Content-Type": contentType } });
  } catch (err: any) {
    return NextResponse.json({ error: err.message || "Unknown error" }, { status: 500 });
  }
}
