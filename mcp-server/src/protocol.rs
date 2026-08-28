use anyhow::{bail, Result};
use chacha20poly1305::{
    aead::{Aead, KeyInit},
    ChaCha20Poly1305, Nonce,
};
use ed25519_dalek::{Signature, Signer, SigningKey, Verifier, VerifyingKey};
use hkdf::Hkdf;
use serde::{Deserialize, Serialize};
use sha2::Sha256;
use std::sync::atomic::{AtomicU64, Ordering};
use x25519_dalek::{EphemeralSecret, PublicKey as X25519Public};

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct SealedMessage {
    pub client_ed25519_pk: String,
    pub client_ephemeral_x25519: String,
    pub nonce: String,
    pub sequence_number: u64,
    pub ciphertext: String,
    pub signature: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct SealedResponse {
    pub server_ephemeral_x25519: String,
    pub nonce: String,
    pub sequence_number: u64,
    pub ciphertext: String,
    pub server_signature: String,
}

pub struct SecurityContext {
    pub signing_key: SigningKey,
    pub verifying_key: VerifyingKey,
    pub last_seen_sequence: AtomicU64,
}

impl SecurityContext {
    pub fn new() -> Self {
        let mut rng = rand::rngs::OsRng;
        let signing_key = SigningKey::generate(&mut rng);
        let verifying_key = signing_key.verifying_key();
        Self {
            signing_key,
            verifying_key,
            last_seen_sequence: AtomicU64::new(0),
        }
    }

    fn derive_key(shared_secret: &[u8; 32], salt: &[u8]) -> [u8; 32] {
        let hk = Hkdf::<Sha256>::new(Some(salt), shared_secret);
        let mut key = [0u8; 32];
        hk.expand(b"MCP-PUBLIC-NET-DEFENSE-V1", &mut key)
            .expect("HKDF expansion failed");
        key
    }

    pub fn process_incoming(
        &self,
        msg: &SealedMessage,
    ) -> Result<(Vec<u8>, VerifyingKey, X25519Public)> {
        let b64 = &base64::engine::general_purpose::STANDARD;
        use base64::Engine;

        // 1. Anti-Replay Verification
        let prev_seq = self.last_seen_sequence.load(Ordering::SeqCst);
        if msg.sequence_number <= prev_seq {
            bail!("Replay attack detected: sequence counter not strictly monotonic");
        }

        // 2. Decode Public Keys and Cryptographic Components
        let client_vk_bytes = b64.decode(&msg.client_ed25519_pk)?;
        let client_vk = VerifyingKey::from_bytes(client_vk_bytes.as_slice().try_into()?)?;

        let client_x25519_bytes: [u8; 32] = b64
            .decode(&msg.client_ephemeral_x25519)?
            .as_slice()
            .try_into()
            .map_err(|_| anyhow::anyhow!("Invalid X25519 public key"))?;
        let client_ephemeral_pk = X25519Public::from(client_x25519_bytes);

        let nonce_bytes: [u8; 12] = b64
            .decode(&msg.nonce)?
            .as_slice()
            .try_into()
            .map_err(|_| anyhow::anyhow!("Invalid nonce size"))?;
        let ciphertext = b64.decode(&msg.ciphertext)?;

        let sig_bytes: [u8; 64] = b64
            .decode(&msg.signature)?
            .as_slice()
            .try_into()
            .map_err(|_| anyhow::anyhow!("Invalid signature size"))?;
        let signature = Signature::from_bytes(&sig_bytes);

        // 3. Authenticate Message Integrity & Origin
        // The signature signs both the sequence number and ciphertext to bind the ordering
        let mut auth_payload = msg.sequence_number.to_be_bytes().to_vec();
        auth_payload.extend_from_slice(&ciphertext);
        client_vk.verify(&auth_payload, &signature)?;

        // 4. Ephemeral Diffie-Hellman Decryption
        let mut rng = rand::rngs::OsRng;
        let server_ephemeral_secret = EphemeralSecret::random_from_rng(&mut rng);
        let shared_secret = server_ephemeral_secret.diffie_hellman(&client_ephemeral_pk);
        let session_key = Self::derive_key(shared_secret.as_bytes(), &nonce_bytes);

        let cipher = ChaCha20Poly1305::new(&session_key.into());
        let nonce = Nonce::from_slice(&nonce_bytes);
        let plaintext = cipher
            .decrypt(nonce, ciphertext.as_ref())
            .map_err(|_| anyhow::anyhow!("AEAD integrity check failed: payload was tampered with"))?;

        // Update sequence state upon successful validation
        self.last_seen_sequence.store(msg.sequence_number, Ordering::SeqCst);

        Ok((plaintext, client_vk, client_ephemeral_pk))
    }

    pub fn seal_response(
        &self,
        client_ephemeral_pk: &X25519Public,
        seq: u64,
        response_data: &[u8],
    ) -> Result<SealedResponse> {
        let mut rng = rand::rngs::OsRng;
        let server_ephemeral_secret = EphemeralSecret::random_from_rng(&mut rng);
        let server_ephemeral_pk = X25519Public::from(&server_ephemeral_secret);

        let mut nonce_bytes = [0u8; 12];
        rand::RngCore::fill_bytes(&mut rng, &mut nonce_bytes);

        let shared_secret = server_ephemeral_secret.diffie_hellman(client_ephemeral_pk);
        let session_key = Self::derive_key(shared_secret.as_bytes(), &nonce_bytes);

        let cipher = ChaCha20Poly1305::new(&session_key.into());
        let nonce = Nonce::from_slice(&nonce_bytes);
        let ciphertext = cipher
            .encrypt(nonce, response_data)
            .map_err(|_| anyhow::anyhow!("Encryption failed"))?;

        // Sign sequence number + ciphertext for server authenticity
        let mut auth_payload = seq.to_be_bytes().to_vec();
        auth_payload.extend_from_slice(&ciphertext);
        let signature = self.signing_key.sign(&auth_payload);

        let b64 = &base64::engine::general_purpose::STANDARD;
        use base64::Engine;

        Ok(SealedResponse {
            server_ephemeral_x25519: b64.encode(server_ephemeral_pk.as_bytes()),
            nonce: b64.encode(nonce_bytes),
            sequence_number: seq,
            ciphertext: b64.encode(ciphertext),
            server_signature: b64.encode(signature.to_bytes()),
        })
    }
}
