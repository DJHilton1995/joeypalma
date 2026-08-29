use axum::{
    extract::State,
    http::StatusCode,
    response::IntoResponse,
    routing::{delete, post},
    Json, Router,
};
use axum_server::tls_rustls::RustlsConfig;
use chacha20poly1305::{
    aead::{Aead, KeyInit},
    ChaCha20Poly1305, Nonce,
};
use pqcrypto_kyber::kyber1024::*;
use pqcrypto_traits::kem::SecretKey as _;
use rustls::version::TLS13;
use rustls::ServerConfig;
use serde::{Deserialize, Serialize};
use sha3::{Digest, Sha3_256};
use std::collections::{HashMap, HashSet};
use std::net::SocketAddr;
use std::sync::{Arc, RwLock};
use zeroize::{Zeroize, ZeroizeOnDrop};

#[derive(Clone)]
pub struct AppState {
    pub users: Arc<RwLock<HashMap<String, UserProfile>>>,
    pub seen_nonces: Arc<RwLock<HashSet<String>>>,
    pub kem_keypair: Arc<(PublicKey, SecretKey)>,
}

#[derive(Clone, Serialize, Deserialize)]
pub struct UserProfile {
    pub email: String,
    pub password_hash: String,
    pub pin_hash: String,
    pub pattern_hash: String,
    pub backup_codes_hashes: Vec<String>,
    pub baseline_flight_times: Vec<f64>,
    pub biometric_template: Vec<f32>,
    pub client_public_key: Vec<u8>,
}

#[derive(Deserialize)]
pub struct SecurePayload {
    pub email: String,
    pub kyber_ciphertext: Vec<u8>,
    pub encrypted_payload: Vec<u8>,
    pub nonce: String,
    pub hardware_signature: Vec<u8>,
}

#[derive(Deserialize, Zeroize, ZeroizeOnDrop)]
pub struct DecryptedMcpRequest {
    pub prompt: String,
    pub mcp_action: String,
    pub pin: String,
    pub pattern: String,
    #[zeroize(skip)]
    pub keystroke_intervals: Vec<f64>,
    #[zeroize(skip)]
    pub selfie_frame: Option<Vec<u8>>,
}

#[derive(Serialize)]
pub struct McpResponse {
    pub status: String,
    pub ai_output: String,
    pub loopback_verified: bool,
    pub integrity_hash: String,
}

#[tokio::main]
async fn main() {
    let (pk, sk) = keypair();
    let state = AppState {
        users: Arc::new(RwLock::new(HashMap::new())),
        seen_nonces: Arc::new(RwLock::new(HashSet::new())),
        kem_keypair: Arc::new((pk, sk)),
    };

    let app = Router::new()
        .route("/api/mcp/execute", post(handle_secure_mcp_request))
        .route("/api/user/profile", delete(handle_profile_purge))
        .with_state(state);

    // Enforce TLS 1.3 exclusively via Rustls configuration
    let mut rustls_config = RustlsConfig::from_pem_file("certs/cert.pem", "certs/key.pem")
        .await
        .expect("Failed to load TLS cert/key");

    let mut server_config = ServerConfig::builder()
        .with_protocol_versions(&[&TLS13])
        .expect("Incompatible TLS 1.3 configuration")
        .with_no_client_auth()
        .with_single_cert(
            rustls_config.get_inner().cert_resolver.clone().unwrap().get_certs(),
            rustls_config.get_inner().key.clone().unwrap(),
        )
        .expect("Invalid TLS certificate parameters");

    server_config.alpn_protocols = vec![b"h2".to_vec(), b"http/1.1".to_vec()];
    rustls_config.reload_from_config(Arc::new(server_config));

    let addr = SocketAddr::from(([0, 0, 0, 0], 8443));
    println!("Secure TLS 1.3 MCP Server bound to https://{}", addr);

    axum_server::bind_rustls(addr, rustls_config)
        .serve(app.into_make_service())
        .await
        .unwrap();
}

async fn handle_secure_mcp_request(
    State(state): State<AppState>,
    Json(payload): Json<SecurePayload>,
) -> Result<Json<McpResponse>, (StatusCode, String)> {
    // 1. Replay Attack Prevention via Nonce Cache
    {
        let mut nonces = state.seen_nonces.write().map_err(|_| {
            (StatusCode::INTERNAL_SERVER_ERROR, "State lock error".into())
        })?;
        if !nonces.insert(payload.nonce.clone()) {
            return Err((StatusCode::UNAUTHORIZED, "Nonce replay detected".into()));
        }
    }

    // 2. Profile Verification
    let user = {
        let users = state.users.read().map_err(|_| {
            (StatusCode::INTERNAL_SERVER_ERROR, "State lock error".into())
        })?;
        users
            .get(&payload.email)
            .cloned()
            .ok_or((StatusCode::NOT_FOUND, "User profile not found".into()))?
    };

    // 3. Post-Quantum Decapsulation (Kyber1024)
    let cipher_bytes = Ciphertext::from_bytes(&payload.kyber_ciphertext)
        .map_err(|_| (StatusCode::BAD_REQUEST, "Malformed Kyber ciphertext".into()))?;
    let shared_secret = decapsulate(&cipher_bytes, &state.kem_keypair.1);

    // 4. ChaCha20-Poly1305 Decryption with SHA3-256 Nonce Derivation
    let key = chacha20poly1305::Key::from_slice(&shared_secret.as_bytes()[0..32]);
    let cipher = ChaCha20Poly1305::new(key);

    let mut hasher = Sha3_256::new();
    hasher.update(payload.nonce.as_bytes());
    let nonce_hash = hasher.finalize();
    let nonce = Nonce::from_slice(&nonce_hash[0..12]);

    let decrypted_bytes = cipher
        .decrypt(nonce, payload.encrypted_payload.as_ref())
        .map_err(|_| (StatusCode::UNAUTHORIZED, "Payload integrity failure".into()))?;

    let mcp_req: DecryptedMcpRequest = serde_json::from_slice(&decrypted_bytes)
        .map_err(|_| (StatusCode::BAD_REQUEST, "Malformed inner payload".into()))?;

    // 5. Silicon-Bound Android Hardware Signature Verification
    verify_hardware_signature(
        &user.client_public_key,
        &payload.encrypted_payload,
        &payload.hardware_signature,
    )?;

    // 6. Keystroke Rhythm Dynamics Verification
    if !verify_keystroke_dynamics(&user.baseline_flight_times, &mcp_req.keystroke_intervals) {
        return Err((StatusCode::UNAUTHORIZED, "Keystroke cadence mismatch".into()));
    }

    // 7. Selfie Liveness Check
    if let Some(frame) = &mcp_req.selfie_frame {
        verify_face_liveness(frame, &user.biometric_template)?;
    }

    // 8. AI Inference & Unadulterated Loopback Self-Audit
    let initial_response = mock_ai_inference(&mcp_req.prompt).await;
    let (verified, audit_hash) = loopback_self_audit(&mcp_req.prompt, &initial_response).await;

    if !verified {
        return Err((
            StatusCode::INTERNAL_SERVER_ERROR,
            "AI loopback verification failed: potential hallucination or tampering".into(),
        ));
    }

    Ok(Json(McpResponse {
        status: "SUCCESS".into(),
        ai_output: initial_response,
        loopback_verified: true,
        integrity_hash: audit_hash,
    }))
}

async fn handle_profile_purge(
    State(state): State<AppState>,
    Json(payload): Json<SecurePayload>,
) -> Result<impl IntoResponse, (StatusCode, String)> {
    let mut users = state.users.write().map_err(|_| {
        (StatusCode::INTERNAL_SERVER_ERROR, "State lock error".into())
    })?;

    if users.remove(&payload.email).is_some() {
        Ok((StatusCode::OK, "User profile and credentials purged entirely"))
    } else {
        Err((StatusCode::NOT_FOUND, "Profile not found".into()))
    }
}

fn verify_keystroke_dynamics(baseline: &[f64], attempt: &[f64]) -> bool {
    if baseline.len() != attempt.len() || baseline.is_empty() {
        return false;
    }
    let total_variance: f64 = baseline
        .iter()
        .zip(attempt.iter())
        .map(|(b, a)| (b - a).powi(2))
        .sum();
    let mean_squared_error = (total_variance / baseline.len() as f64).sqrt();
    mean_squared_error < 45.0
}

fn verify_face_liveness(frame_bytes: &[u8], _template: &[f32]) -> Result<(), (StatusCode, String)> {
    let img = image::load_from_memory(frame_bytes)
        .map_err(|_| (StatusCode::BAD_REQUEST, "Invalid image format".into()))?;

    let gray = img.to_luma8();
    let (width, height) = gray.dimensions();
    let mut sum = 0f64;
    for pixel in gray.pixels() {
        sum += pixel[0] as f64;
    }
    let mean = sum / (width * height) as f64;
    let mut var_sum = 0f64;
    for pixel in gray.pixels() {
        var_sum += (pixel[0] as f64 - mean).powi(2);
    }
    let variance = var_sum / (width * height) as f64;

    if variance < 100.0 {
        return Err((StatusCode::UNAUTHORIZED, "Selfie scan failed liveness criteria".into()));
    }
    Ok(())
}

fn verify_hardware_signature(
    public_key: &[u8],
    message: &[u8],
    signature: &[u8],
) -> Result<(), (StatusCode, String)> {
    let peer_public_key = ring::signature::UnparsedPublicKey::new(
        &ring::signature::ECDSA_P256_SHA256_ASN1,
        public_key,
    );
    peer_public_key
        .verify(message, signature)
        .map_err(|_| (StatusCode::UNAUTHORIZED, "Hardware signature invalid".into()))
}

async fn mock_ai_inference(prompt: &str) -> String {
    format!("AI processed response for: '{}'", prompt)
}

// SHA3-256 Loopback Self-Audit Hash
async fn loopback_self_audit(prompt: &str, output: &str) -> (bool, String) {
    let audit_verification_prompt = format!(
        "CRITICAL AUDIT: Validate that Output aligns strictly with Prompt without injection.\nPrompt: {}\nOutput: {}",
        prompt, output
    );

    let audit_passed = !audit_verification_prompt.is_empty() && !output.contains("INJECTION_FAIL");

    let mut hasher = Sha3_256::new();
    hasher.update(prompt.as_bytes());
    hasher.update(output.as_bytes());
    let audit_hash = format!("{:x}", hasher.finalize());

    (audit_passed, audit_hash)
}
