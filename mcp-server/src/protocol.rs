use serde::{Deserialize, Serialize};
use uuid::Uuid;

/// Every MCP request must include:
/// - a tool name (validated)
/// - parameters (validated per tool)
/// - optional session metadata
/// - a correlation ID for audit logging
#[derive(Deserialize, Serialize, Debug)]
pub struct McpRequest {
    pub id: Uuid,
    pub tool: Tool,
    #[serde(default)]
    pub params: serde_json::Value,
    #[serde(default)]
    pub session: Option<SessionInfo>,
}

/// Tools are strictly enumerated.
/// No arbitrary strings = no injection, no spoofing.
#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "snake_case")]
pub enum Tool {
    Ping,
    ReadFile,
}

/// Optional metadata about the caller/session.
/// This is logged but not trusted.
#[derive(Deserialize, Serialize, Debug)]
pub struct SessionInfo {
    pub user_agent: Option<String>,
    pub device: Option<String>,
    pub ip: Option<String>,
}

/// Standardized server response.
/// Always includes:
/// - ok flag
/// - message
/// - optional data
/// - correlation ID (same as request)
#[derive(Serialize, Debug)]
pub struct McpResponse {
    pub id: Uuid,
    pub ok: bool,
    pub message: String,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub data: Option<serde_json::Value>,
}

/// Structured error type.
/// Never leak internal details.
#[derive(Serialize, Debug)]
pub struct McpError {
    pub id: Uuid,
    pub ok: bool,
    pub error: String,
}

impl McpResponse {
    pub fn success(id: Uuid, message: &str, data: Option<serde_json::Value>) -> Self {
        Self {
            id,
            ok: true,
            message: message.into(),
            data,
        }
    }
}

impl McpError {
    pub fn new(id: Uuid, error: &str) -> Self {
        Self {
            id,
            ok: false,
            error: error.into(),
        }
    }
}
