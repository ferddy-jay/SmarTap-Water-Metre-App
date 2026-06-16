package com.example.myfirstapp

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import com.example.myfirstapp.data.Tenant
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myfirstapp.data.AppDatabase
import com.example.myfirstapp.data.User
import com.example.myfirstapp.ui.theme.MyFirstAppTheme
import com.example.myfirstapp.ui.theme.components.DashboardCard
import com.example.myfirstapp.ui.theme.login_screen.LoginScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                    val coroutineScope = rememberCoroutineScope()
                    val context = LocalContext.current

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
                                        val user = withContext(Dispatchers.IO) {
                                            val database = AppDatabase.getDatabase(context)
                                            database.userDao().getUserByEmail(email)
                                        }
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
                                        val result = withContext(Dispatchers.IO) {
                                            val database = AppDatabase.getDatabase(context)
                                            val userDao = database.userDao()
                                            val existingUser = userDao.getUserByEmail(email)
                                            if (existingUser == null) {
                                                userDao.insertUser(User(email = email, password = password))
                                                true
                                            } else {
                                                false
                                            }
                                        }
                                        if (result) {
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
                                onNavigateToTenants = { navController.navigate("tenants") },
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
                        composable("tenants") {
                            TenantListScreen(
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
fun RegisterScreen(onRegisterSuccess: (String, String) -> Unit, onNavigateToLogin: () -> Unit, modifier: Modifier = Modifier) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isEmailError by remember { mutableStateOf(false) }
    var isPasswordMismatch by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
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
                // Simplified validation to match optimized LoginScreen
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
fun HomeScreen(
    name: String,
    onNavigateToDashboard: () -> Unit,
    onNavigateToDetails: () -> Unit,
    onNavigateToTenants: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
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

        // New Water Metre / Tenant Section
        var userName by remember { mutableStateOf("") }
        var apartmentName by remember { mutableStateOf("") }
        var waterCompany by remember { mutableStateOf("") }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Tenant Registration", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("User Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
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
                    label = { Text("Metre Number (e.g. A12345)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = apartmentName,
                    onValueChange = { apartmentName = it },
                    label = { Text("Apartment Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = waterCompany,
                    onValueChange = { waterCompany = it },
                    label = { Text("Water Company") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))
                
                val scope = rememberCoroutineScope()
                Button(
                    onClick = {
                        if (userName.isNotEmpty() && metreNumber.length == 6 && apartmentName.isNotEmpty()) {
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    val db = AppDatabase.getDatabase(context)
                                    db.tenantDao().insertTenant(
                                        Tenant(
                                            userName = userName,
                                            metreNumber = metreNumber,
                                            apartmentName = apartmentName,
                                            waterCompany = waterCompany
                                        )
                                    )
                                }
                                Toast.makeText(context, "Details for $userName saved", Toast.LENGTH_SHORT).show()
                                userName = ""
                                metreNumber = ""
                                apartmentName = ""
                                waterCompany = ""
                            }
                        } else {
                            Toast.makeText(context, "Please fill in all details correctly", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Details")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onNavigateToTenants,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Group, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("View Stored Tenants")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
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
fun DashboardScreen(onNavigateBack: () -> Unit, modifier: Modifier = Modifier) {
    val items = remember {
        listOf<Pair<String, ImageVector>>(
            "Recent Activity" to Icons.Default.History,
            "System Health" to Icons.Default.HealthAndSafety,
            "User Analytics" to Icons.Default.Analytics,
            "Reports" to Icons.Default.Assessment
        )
    }

    Column(modifier = modifier.fillMaxSize().padding(24.dp)) {
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


@Composable
fun TenantListScreen(onNavigateBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val tenants by database.tenantDao().getAllTenants().collectAsState(initial = emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Stored Tenants", style = MaterialTheme.typography.headlineMedium)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (tenants.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No tenants stored locally.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(tenants) { tenant ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = tenant.userName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(text = "Metre: ${tenant.metreNumber}", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Apartment: ${tenant.apartmentName}", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Water Co: ${tenant.waterCompany}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
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
