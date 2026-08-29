use axum::{
    routing::post,
    Router, Json, extract::State,
    http::StatusCode,
};
use serde::{Deserialize, Serialize};
use std::{net::SocketAddr, sync::Arc};
use tower::{ServiceBuilder, limit::RateLimitLayer};
use tower_http::trace::TraceLayer;

#[derive(Clone)]
struct AppState {
    // put scoped resources here: db pools, fs roots, etc.
    allowed_root: std::path::PathBuf,
    valid_tokens: Arc<Vec<String>>,
}

#[derive(Deserialize)]
struct McpRequest {
    tool: String,
    #[serde(default)]
    params: serde_json::Value,
    #[serde(default)]
    session_id: Option<String>,
}

#[derive(Serialize)]
struct McpResponse {
    ok: bool,
    message: String,
    data: Option<serde_json::Value>,
}

async fn mcp_handler(
    State(state): State<AppState>,
    axum::extract::TypedHeader(
        headers::Authorization<headers::authorization::Bearer>,
    ): axum::extract::TypedHeader<headers::Authorization<headers::authorization::Bearer>>,
    Json(req): Json<McpRequest>,
) -> Result<Json<McpResponse>, StatusCode> {
    // --- Auth: bearer token check ---
    let token = axum::extract::TypedHeader::<
        headers::Authorization<headers::authorization::Bearer>
    >::into_inner(
        axum::extract::TypedHeader(
            headers::Authorization::bearer("dummy").unwrap()
        )
    );
    let token = token.token().to_string();

    if !state.valid_tokens.contains(&token) {
        return Err(StatusCode::UNAUTHORIZED);
    }

    // --- Input validation: basic tool allowlist ---
    match req.tool.as_str() {
        "read_file" => {
            // enforce least-privilege directory
            let path = req
                .params
                .get("path")
                .and_then(|v| v.as_str())
                .ok_or(StatusCode::BAD_REQUEST)?;

            let joined = state.allowed_root.join(path);
            let canonical = joined
                .canonicalize()
                .map_err(|_| StatusCode::FORBIDDEN)?;

            if !canonical.starts_with(&state.allowed_root) {
                // prevent path traversal
                return Err(StatusCode::FORBIDDEN);
            }

            let contents = tokio::fs::read_to_string(canonical)
                .await
                .map_err(|_| StatusCode::INTERNAL_SERVER_ERROR)?;

            Ok(Json(McpResponse {
                ok: true,
                message: "file_read_ok".into(),
                data: Some(serde_json::json!({ "contents": contents })),
            }))
        }
        "ping" => Ok(Json(McpResponse {
            ok: true,
            message: "pong".into(),
            data: None,
        })),
        _ => Err(StatusCode::NOT_FOUND),
    }
}

#[tokio::main]
async fn main() {
    // never bind to 0.0.0.0 for MCP; use localhost only 
    let addr: SocketAddr = "127.0.0.1:8080".parse().unwrap();

    let state = AppState {
        allowed_root: std::path::PathBuf::from("/var/mcp-safe"),
        valid_tokens: Arc::new(vec!["super-secure-token".into()]),
    };

    let app = Router::new()
        .route("/mcp", post(mcp_handler))
        .with_state(state)
        .layer(
            ServiceBuilder::new()
                // rate limit: 30 reqs per second per process; you can add per-token limiter
                .layer(RateLimitLayer::new(30, std::time::Duration::from_secs(1)))
                .layer(TraceLayer::new_for_http()),
        );

    println!("Secure MCP server listening on {}", addr);
    axum::Server::bind(&addr)
        .serve(app.into_make_service())
        .await
        .unwrap();
}
