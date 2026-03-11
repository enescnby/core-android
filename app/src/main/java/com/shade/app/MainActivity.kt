package com.shade.app

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.shade.app.crypto.AuthCryptoManager
import com.shade.app.model.RegisterRequest
import com.shade.app.network.AuthApi
import com.shade.app.security.KeyVaultManager
import com.shade.app.ui.screens.LoginScreen
import com.shade.app.ui.screens.RegisterScreen
import com.shade.app.ui.theme.ShadeTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authCryptoManager = AuthCryptoManager()
        val keyVaultManager = KeyVaultManager(applicationContext)

        setContent {
            ShadeTheme {
                var currentScreen by remember { mutableStateOf("login") }

                when (currentScreen) {
                    "login" -> LoginScreen(
                        onNavigateToRegister = {
                            currentScreen = "register"
                        }
                    )

                    "register" -> RegisterScreen(
                        onNavigateToLogin = {
                            currentScreen = "login"
                        },
                        onRegisterClick = { _, _, _ ->
                            try {
                                withContext(Dispatchers.IO) {
                                    val (publicKeyHex, privateKeyHex) =
                                        authCryptoManager.generateEd25519KeyPairHex()

                                    keyVaultManager.saveEd25519PrivateKey(privateKeyHex)

                                    val request = RegisterRequest(
                                        public_key = publicKeyHex,
                                        encrypted_private_key = privateKeyHex,
                                        salt = "dummy",
                                        device_model = Build.MODEL ?: "Android Device",
                                        fcm_token = "dummy"
                                    )

                                    val response = AuthApi.register(request)
                                    "Kayıt başarılı. CoreGuard ID: ${response.core_guard_id}"
                                }
                            } catch (e: Exception) {
                                "Hata: ${e.javaClass.simpleName} - ${e.message}"
                            }
                        }
                    )
                }
            }
        }
    }
}