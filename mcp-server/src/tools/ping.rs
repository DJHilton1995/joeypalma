use uuid::Uuid;
use crate::protocol::McpResponse;

pub async fn handle(id: Uuid) -> McpResponse {
    McpResponse::success(id, "pong", None)
}
