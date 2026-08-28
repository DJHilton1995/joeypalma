import { NextResponse } from "next/server";

export async function POST(req: Request) {
  try {
    const body = await req.json();
    const claims: string[] = body.claims || [];
    if (!Array.isArray(claims) || claims.length === 0) {
      return NextResponse.json({ error: "No claims provided" }, { status: 400 });
    }

    // Simple prototype: ask MCP to verify each claim. This forwards to MCP route.
    const MCP_URL = process.env.MCP_URL;
    const MCP_TOKEN = process.env.MCP_TOKEN;
    if (!MCP_URL || !MCP_TOKEN) {
      return NextResponse.json({ error: "MCP_URL or MCP_TOKEN not configured" }, { status: 500 });
    }

    const forwardBody = {
      id: crypto.randomUUID(),
      tool: "fact_check",
      params: { claims },
    };

    const resp = await fetch(MCP_URL, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${MCP_TOKEN}`,
      },
      body: JSON.stringify(forwardBody),
    });

    const data = await resp.json();
    return NextResponse.json({ result: data }, { status: resp.status });
  } catch (err: any) {
    return NextResponse.json({ error: err.message || "Unknown error" }, { status: 500 });
  }
}
