package com.shade.app.domain.usecase

import com.shade.app.crypto.AuthCryptoManager
import com.shade.app.crypto.MessageCryptoManager
import com.shade.app.domain.repository.AuthRepository
import org.bouncycastle.util.encoders.Hex
import java.security.SecureRandom

class RegisterUseCase(
    private val repository: AuthRepository,
    private val authCrypto: AuthCryptoManager,
    private val messageCrypto: MessageCryptoManager
) {
    suspend operator fun invoke(mnemonic: List<String>, deviceModel: String, fcmToken: String): Result<String> {
        return try {
            val (idPub, idPriv) = authCrypto.generateEd25519KeyPairHex()
            val (encPub, encPriv) = messageCrypto.generateX25519KeyPairHex()

            val saltBytes = ByteArray(16)
            SecureRandom().nextBytes(saltBytes)
            val saltHex = Hex.toHexString(saltBytes)

            val aesKey = authCrypto.deriveAesKeyFromMnemonic(mnemonic, saltHex)

            val encryptedIdPriv = authCrypto.encryptPrivateKey(idPriv, aesKey)
            val encryptedEncPriv = authCrypto.encryptPrivateKey(encPriv, aesKey)

            repository.register(
                identityPublicKey = idPub,
                encryptedIdentityPrivateKey = encryptedIdPriv,
                encryptionPublicKey = encPub,
                encryptedEncryptionPrivateKey = encryptedEncPriv,
                salt = saltHex,
                deviceModel = deviceModel,
                fcmToken = fcmToken
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}