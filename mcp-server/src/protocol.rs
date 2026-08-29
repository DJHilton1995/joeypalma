use serde::{Deserialize, Serialize};
use uuid::Uuid;

#[derive(Deserialize, Serialize, Debug)]
pub struct McpRequest {
    pub id: Uuid,
    pub tool: Tool,
    #[serde(default)]
    pub params: serde_json::Value,
    #[serde(default)]
    pub session: Option<SessionInfo>,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "snake_case")]
pub enum Tool {
    Ping,
    ReadFile,
}

#[derive(Deserialize, Serialize, Debug)]
pub struct SessionInfo {
    pub user_agent: Option<String>,
    pub device: Option<String>,
    pub ip: Option<String>,
}

#[derive(Serialize, Debug)]
pub struct McpResponse {
    pub id: Uuid,
    pub ok: bool,
    pub message: String,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub data: Option<serde_json::Value>,
}

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
