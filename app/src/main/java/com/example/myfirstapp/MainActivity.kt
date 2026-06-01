package com.example.myfirstapp

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myfirstapp.data.AppDatabase
import com.example.myfirstapp.data.User
import com.example.myfirstapp.ui.theme.MyFirstAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyFirstAppTheme {
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    val context = LocalContext.current
                    val database = AppDatabase.getDatabase(context)
                    val userDao = database.userDao()
                    val coroutineScope = rememberCoroutineScope()

                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(innerPadding),
                        enterTransition = { fadeIn(animationSpec = tween(400)) + slideInHorizontally(initialOffsetX = { 300 }) },
                        exitTransition = { fadeOut(animationSpec = tween(400)) + slideOutHorizontally(targetOffsetX = { -300 }) },
                        popEnterTransition = { fadeIn(animationSpec = tween(400)) + slideInHorizontally(initialOffsetX = { -300 }) },
                        popExitTransition = { fadeOut(animationSpec = tween(400)) + slideOutHorizontally(targetOffsetX = { 300 }) }
                    ) {
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = { email, password ->
                                    coroutineScope.launch {
                                        val user = userDao.getUserByEmail(email)
                                        if (user != null && user.password == password) {
                                            navController.navigate("home") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        } else {
                                            Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onNavigateToRegister = { navController.navigate("register") }
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                onRegisterSuccess = { email, password ->
                                    coroutineScope.launch {
                                        val existingUser = userDao.getUserByEmail(email)
                                        if (existingUser == null) {
                                            userDao.insertUser(User(email = email, password = password))
                                            Toast.makeText(context, "Registration successful", Toast.LENGTH_SHORT).show()
                                            navController.navigate("login")
                                        } else {
                                            Toast.makeText(context, "User already exists", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onNavigateToLogin = { navController.navigate("login") }
                            )
                        }
                        composable("home") {
                            HomeScreen(
                                name = "User",
                                onNavigateToDashboard = { navController.navigate("dashboard") },
                                onNavigateToDetails = { navController.navigate("details") },
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("dashboard") {
                            DashboardScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("details") {
                            DetailsScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

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
                isEmailError = it.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(it).matches()
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

@Composable
fun RegisterScreen(onRegisterSuccess: (String, String) -> Unit, onNavigateToLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isEmailError by remember { mutableStateOf(false) }
    var isPasswordMismatch by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it 
                isEmailError = it.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(it).matches()
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
            onValueChange = { 
                password = it 
                isPasswordMismatch = confirmPassword.isNotEmpty() && it != confirmPassword
            },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { 
                confirmPassword = it 
                isPasswordMismatch = password.isNotEmpty() && it != password
            },
            label = { Text("Confirm Password") },
            leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
            isError = isPasswordMismatch,
            supportingText = {
                if (isPasswordMismatch) {
                    Text("Passwords do not match", color = MaterialTheme.colorScheme.error)
                }
            },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty() && password == confirmPassword && !isEmailError) {
                    onRegisterSuccess(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Register")
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account? Login")
        }
    }
}

@Composable
fun HomeScreen(name: String, onNavigateToDashboard: () -> Unit, onNavigateToDetails: () -> Unit, onLogout: () -> Unit, modifier: Modifier = Modifier) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var metreNumber by remember { mutableStateOf("") }
    val context = LocalContext.current

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hello, $name!",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Settings") },
                        onClick = { showMenu = false },
                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Logout") },
                        onClick = { 
                            showMenu = false
                            showLogoutDialog = true 
                        },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) }
                    )
                }
            }
        }
        Text(
            text = "Good to see you today.",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(24.dp))

        // New Water Metre Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Water Metre", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = metreNumber,
                    onValueChange = { input ->
                        val clean = input.filter { !it.isWhitespace() }.take(6)
                        if (clean.isEmpty()) {
                            metreNumber = ""
                        } else {
                            val firstChar = clean[0]
                            if (firstChar.isLetter()) {
                                val rest = clean.substring(1).filter { it.isDigit() }
                                metreNumber = firstChar.uppercaseChar() + rest
                            }
                        }
                    },
                    label = { Text("Enter Metre Number") },
                    placeholder = { Text("e.g. A12345") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = {
                        Text("Format: 1 Letter + 5 Digits (${metreNumber.length}/6)")
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (metreNumber.length == 6) {
                            Toast.makeText(context, "Checking activities for metre: $metreNumber", Toast.LENGTH_LONG).show()
                            onNavigateToDashboard()
                        } else {
                            Toast.makeText(context, "Please enter a valid metre number (e.g. A12345)", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("My metre")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Your Stats", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("You have 5 new notifications and 2 pending tasks.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onNavigateToDashboard) {
                    Text("View Dashboard")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedButton(
            onClick = onNavigateToDetails,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Profile Details")
        }
    }
}

@Composable
fun DashboardScreen(onNavigateBack: () -> Unit) {
    val items = listOf<Pair<String, ImageVector>>(
        "Recent Activity" to Icons.Default.History,
        "System Health" to Icons.Default.HealthAndSafety,
        "User Analytics" to Icons.Default.Analytics,
        "Reports" to Icons.Default.Assessment
    )

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Dashboard", style = MaterialTheme.typography.headlineMedium)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items) { (title, icon) ->
                DashboardCard(title = title, icon = icon)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardCard(title: String, icon: ImageVector) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { isExpanded = !isExpanded },
                onLongClick = { 
                    Toast.makeText(context, "Long pressed: $title", Toast.LENGTH_SHORT).show()
                }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).animateContentSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Detailed info for $title would be shown here. This expansion is animated using animateContentSize().",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun DetailsScreen(onNavigateBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Profile Details", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "This screen displays your profile and account settings. You can manage your preferences here.",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
