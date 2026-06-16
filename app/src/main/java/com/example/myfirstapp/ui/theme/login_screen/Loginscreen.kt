package com.example.myfirstapp.ui.theme.login_screen

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(onLoginSuccess: (String, String) -> Unit, onNavigateToRegister: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isEmailError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                // Simplified validation to prevent regex hang on slow emulators
                isEmailError = it.isNotEmpty() && (!it.contains("@") || !it.contains("."))
            },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            isError = isEmailError,
            supportingText = {
                if (isEmailError) {
                    Text("Invalid email format", color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Password, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty() && !isEmailError) {
                    onLoginSuccess(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Login")
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Register")
        }
    }
}
