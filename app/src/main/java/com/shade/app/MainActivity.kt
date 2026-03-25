package com.shade.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.shade.app.crypto.AuthCryptoManager
import com.shade.app.crypto.MessageCryptoManager
import com.shade.app.crypto.MnemonicManager
import com.shade.app.data.remote.api.AuthService
import com.shade.app.data.repository.AuthRepositoryImpl
import com.shade.app.domain.usecase.LoginUseCase
import com.shade.app.domain.usecase.RegisterUseCase
import com.shade.app.security.KeyVaultManager
import com.shade.app.ui.auth.AuthScreen
import com.shade.app.ui.auth.AuthViewModel
import com.shade.app.ui.theme.ShadeTheme
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            Log.d("FCM", "Notification permission granted: $isGranted")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        askNotificationPermission()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val authService = retrofit.create(AuthService::class.java)

        val repository = AuthRepositoryImpl(authService)
        val authCrypto = AuthCryptoManager()
        val messageCrypto = MessageCryptoManager()
        val mnemonicManager = MnemonicManager()
        val keyVault = KeyVaultManager(this)

        val registerUseCase = RegisterUseCase(repository, authCrypto, messageCrypto)
        val loginUseCase = LoginUseCase(repository, authCrypto)

        val viewModel = AuthViewModel(registerUseCase, loginUseCase, keyVault, mnemonicManager)

        setContent {
            ShadeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AuthScreen(viewModel)
                }
            }
        }

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM", "Token: ${task.result}")
                } else {
                    Log.e("FCM", "Token alınamadı", task.exception)
                }
            }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}