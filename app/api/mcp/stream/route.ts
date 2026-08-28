// app/api/mcp/stream/route.ts
import { NextResponse } from "next/server";

export async function POST(req: Request) {
  try {
    const body = await req.json();
    const MCP_URL = process.env.MCP_URL;
    const MCP_TOKEN = process.env.MCP_TOKEN;

    if (!MCP_URL || !MCP_TOKEN) {
      return NextResponse.json({ error: "MCP_URL or MCP_TOKEN not configured" }, { status: 500 });
    }

    // Forward request to MCP. Expect MCP to stream chunked text (SSE or newline-delimited JSON).
    const upstream = await fetch(MCP_URL, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${MCP_TOKEN}`,
        "Accept": "text/event-stream, application/json, text/plain",
      },
      body: JSON.stringify({
        id: crypto.randomUUID(),
        tool: body.tool,
        params: body.params || {},
        stream: true, // hint to MCP to stream if supported
      }),
    });

    // If upstream isn't streaming, just forward the full body
    if (!upstream.body) {
      const text = await upstream.text();
      return new NextResponse(text, { status: upstream.status, headers: { "Content-Type": upstream.headers.get("content-type") || "text/plain" }});
    }

    // Stream upstream body to client
    const reader = upstream.body.getReader();
    const stream = new ReadableStream({
      async pull(controller) {
        try {
          const { done, value } = await reader.read();
          if (done) {
            controller.close();
            return;
          }
          controller.enqueue(value);
        } catch (err) {
          controller.error(err);
        }
      },
      cancel() {
        reader.cancel();
      }
    });

    const headers = new Headers();
    headers.set("Content-Type", upstream.headers.get("content-type") || "text/event-stream");
    // Allow streaming to be consumed by browser fetch
    return new NextResponse(stream, { headers });
  } catch (err: any) {
    return NextResponse.json({ error: err.message || "Unknown error" }, { status: 500 });
  }
}
