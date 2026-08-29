import { NextResponse } from "next/server";

export async function POST(req: Request) {
  try {
    const body = await req.json();
    const MCP_URL = process.env.MCP_URL;
    const MCP_TOKEN = process.env.MCP_TOKEN;

    if (!MCP_URL || !MCP_TOKEN) {
      return NextResponse.json({ error: "MCP_URL or MCP_TOKEN not configured" }, { status: 500 });
    }

    const forwardBody = {
      id: crypto.randomUUID(),
      tool: body.tool,
      params: body.params || {},
      stream: true,
      system: `You are Joey, a helpful, concise, and human-like cybersecurity assistant.
Prioritize accuracy and cite sources when making factual claims.
If uncertain, say so and offer how to verify.
Keep tone friendly, confident, and conversational.
Avoid hallucinations; prefer "I don't know" over inventing facts.`,
      response_requirements: {
        include_reasoning: true,
        include_sources: true,
        score_dimensions: ["intelligence","complexity","accuracy","validity","humanity"],
        scoring_scale: { min: 0, max: 10 },
        prefer_streamed_json: true
      }
    };

    const upstream = await fetch(MCP_URL, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${MCP_TOKEN}`,
        "Accept": "text/event-stream, application/json, text/plain",
      },
      body: JSON.stringify(forwardBody),
    });

    if (!upstream.body) {
      const text = await upstream.text();
      return new NextResponse(text, { status: upstream.status, headers: { "Content-Type": upstream.headers.get("content-type") || "text/plain" }});
    }

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
    return new NextResponse(stream, { headers });
  } catch (err: any) {
    return NextResponse.json({ error: err.message || "Unknown error" }, { status: 500 });
  }
}
