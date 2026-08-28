use uuid::Uuid;
use crate::protocol::{McpResponse, McpError};
use serde_json::json;
use std::path::Path;

pub async fn handle(
    id: Uuid,
    root: &Path,
    params: serde_json::Value,
) -> Result<McpResponse, McpError> {
    let Some(path) = params.get("path").and_then(|v| v.as_str()) else {
        return Err(McpError::new(id, "missing_path"));
    };

    let joined = root.join(path);
    let canonical = joined
        .canonicalize()
        .map_err(|_| McpError::new(id, "invalid_path"))?;

    if !canonical.starts_with(root) {
        return Err(McpError::new(id, "forbidden"));
    }

    let contents = tokio::fs::read_to_string(canonical)
        .await
        .map_err(|_| McpError::new(id, "read_failed"))?;

    Ok(McpResponse::success(
        id,
        "file_read_ok",
        Some(json!({ "contents": contents })),
    ))
}
