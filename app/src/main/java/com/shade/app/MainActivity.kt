package com.shade.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.shadeapp.tech/api/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val authService =  retrofit.create(AuthService::class.java)

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
    }
}