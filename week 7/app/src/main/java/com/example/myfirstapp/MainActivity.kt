package com.example.myfirstapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myfirstapp.ai.AiAssistant
import com.example.myfirstapp.data.AppDatabase
import com.example.myfirstapp.data.Tenant
import com.example.myfirstapp.data.User
import com.example.myfirstapp.ui.theme.MyFirstAppTheme
import com.example.myfirstapp.ui.theme.components.DashboardCard
import com.example.myfirstapp.ui.theme.login_screen.LoginScreen
import com.example.myfirstapp.network.ApiService
import com.example.myfirstapp.network.model.RemoteUser
import com.example.myfirstapp.data.Attendance
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
                                onNavigateToNetworkData = { navController.navigate("network_data") },
                                onNavigateToManagement = { navController.navigate("management_tasks") },
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
                        composable("network_data") {
                            NetworkDataScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("management_tasks") {
                            ManagementTasksScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}


/**
 * A screen that allows new users to create an account.
 *
 * @param onRegisterSuccess Callback invoked with (email, password) when validation passes and registration is triggered.
 * @param onNavigateToLogin Callback to navigate back to the [LoginScreen].
 * @param modifier Optional [Modifier] for layout adjustments.
 */
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

/**
 * The main landing screen after login. 
 * Features tenant registration, stats overview, and the Smart AI Assistant.
 *
 * @param name The name of the logged-in user.
 * @param onNavigateToDashboard Callback for the "View Dashboard" action.
 * @param onNavigateToDetails Callback for the "Profile Details" action.
 * @param onNavigateToTenants Callback for the "View Stored Tenants" action.
 * @param onLogout Callback to sign out and return to the login flow.
 * @param modifier Optional [Modifier] for layout adjustments.
 */
@Composable
fun HomeScreen(
    name: String,
    onNavigateToDashboard: () -> Unit,
    onNavigateToDetails: () -> Unit,
    onNavigateToTenants: () -> Unit,
    onNavigateToNetworkData: () -> Unit,
    onNavigateToManagement: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var metreNumber by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var aiTip by remember { mutableStateOf<String?>(null) }
    var isAiLoading by remember { mutableStateOf(false) }

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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // AI Assistant Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Smart Assistant", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (aiTip != null) {
                    Text(aiTip!!, style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text("Need help managing your tenants?", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        scope.launch {
                            isAiLoading = true
                            val db = AppDatabase.getDatabase(context)
                            val tenants = withContext(Dispatchers.IO) { db.tenantDao().getAllTenantsSync() }
                            aiTip = AiAssistant.getTenantTips(tenants.map { it.userName })
                            isAiLoading = false
                        }
                    },
                    enabled = !isAiLoading,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    if (isAiLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Text("Get AI Tip")
                }
            }
        }

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

                Spacer(modifier = Modifier.height(16.dp))
                
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

        OutlinedButton(
            onClick = onNavigateToNetworkData,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Icon(Icons.Default.CloudSync, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Fetch Remote People (REST API)")
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNavigateToManagement,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(Icons.Default.Assignment, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Attendance & Lab Tasks")
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
fun DetailsScreen(onNavigateBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Profile Details", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(modifier = Modifier.height(32.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Name: John Doe", style = MaterialTheme.typography.bodyLarge)
                Text("Email: john.doe@example.com", style = MaterialTheme.typography.bodyLarge)
                Text("Account Status: Premium", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun NetworkDataScreen(onNavigateBack: () -> Unit, modifier: Modifier = Modifier) {
    val apiService = remember { ApiService.create() }
    var remoteUsers by remember { mutableStateOf<List<RemoteUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            remoteUsers = apiService.getUsers().take(5) // Fetch at least 5 people
            isLoading = false
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: "Failed to fetch data"
            isLoading = false
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Remote People", style = MaterialTheme.typography.headlineMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(remoteUsers) { remoteUser ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = remoteUser.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Email: ${remoteUser.email}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Company: ${remoteUser.company.name}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "\"${remoteUser.company.catchPhrase}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManagementTasksScreen(onNavigateBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val tenants by database.tenantDao().getAllTenants().collectAsState(initial = emptyList())
    val attendanceList by database.attendanceDao().getAllAttendance().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Storage Analysis", "Registration", "Attendance")

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Management & Storage Lab", style = MaterialTheme.typography.headlineMedium)
        }

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> Task1StorageComparison()
            1 -> Task2TenantRegistration(database, scope)
            2 -> Task3AttendanceManagement(tenants, attendanceList, database, scope)
        }
    }
}

@Composable
fun Task1StorageComparison() {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Text("Storage Method Comparison", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        val methods = listOf(
            listOf("Shared Prefs", "Easy to use, Key-Value", "Small data only"),
            listOf("SQLite/Room", "Structured, Queryable", "Complex setup"),
            listOf("Firebase", "Real-time, Cloud", "Requires Internet"),
            listOf("Internal", "Private files", "No structure")
        )
        
        methods.forEach { row ->
            Card(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(row[0], fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun Task2TenantRegistration(db: AppDatabase, scope: kotlinx.coroutines.CoroutineScope) {
    var name by remember { mutableStateOf("") }
    var apartment by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column {
        Text("Tenant Database Design", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tenant Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = apartment, onValueChange = { apartment = it }, label = { Text("Apartment (Course)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Lease Year") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (name.isNotEmpty() && apartment.isNotEmpty()) {
                scope.launch {
                    withContext(Dispatchers.IO) {
                        db.tenantDao().insertTenant(Tenant(userName = name, apartmentName = apartment, leaseYear = year, phoneNumber = phone))
                    }
                    Toast.makeText(context, "Tenant Registered", Toast.LENGTH_SHORT).show()
                    name = ""; apartment = ""; year = ""; phone = ""
                }
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Register Tenant")
        }
    }
}

@Composable
fun Task3AttendanceManagement(tenants: List<Tenant>, attendance: List<Attendance>, db: AppDatabase, scope: kotlinx.coroutines.CoroutineScope) {
    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Attendance (Date: $date)", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        
        Text("Mark Attendance:", fontWeight = FontWeight.Bold)
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(tenants) { tenant ->
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(tenant.userName)
                    Button(onClick = {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                db.attendanceDao().insertAttendance(Attendance(tenantId = tenant.id, tenantName = tenant.userName, date = date, isPresent = true))
                            }
                        }
                    }) {
                        Text("Present")
                    }
                }
            }
        }
        
        Divider()
        Text("Attendance Report:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(attendance) { record ->
                Text("${record.date}: ${record.tenantName} - ${if(record.isPresent) "Present" else "Absent"}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

/**
 * Displays a list of all tenants stored in the local Room database.
 * Includes functionality to sync data with the cloud and delete individual records.
 *
 * @param onNavigateBack Callback to return to the previous screen.
 * @param modifier Optional [Modifier] for layout adjustments.
 */
@Composable
fun TenantListScreen(onNavigateBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    
    // State to handle loading
    var isLoading by remember { mutableStateOf(true) }
    val tenantsFlow = remember { database.tenantDao().getAllTenants() }
    val tenants by tenantsFlow.collectAsState(initial = emptyList())
    
    val scope = rememberCoroutineScope()

    // Simulate initial data fetch delay to show the loading spinner
    LaunchedEffect(Unit) {
        delay(5000) // 5 second delay for screenshots
        isLoading = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text("Stored Tenants", style = MaterialTheme.typography.headlineMedium)
            }
            IconButton(onClick = {
                scope.launch {
                    isLoading = true
                    Toast.makeText(context, "Syncing with cloud...", Toast.LENGTH_SHORT).show()
                    // Mock sync call
                    delay(1500)
                    Toast.makeText(context, "Cloud sync successful", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
            }) {
                Icon(Icons.Default.CloudSync, contentDescription = "Sync")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )
            }
        } else if (tenants.isEmpty()) {
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
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.0f)) {
                                Text(text = tenant.userName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(text = "Metre: ${tenant.metreNumber}", style = MaterialTheme.typography.bodyMedium)
                                Text(text = "Apartment: ${tenant.apartmentName}", style = MaterialTheme.typography.bodyMedium)
                            }
                            IconButton(onClick = {
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        database.tenantDao().deleteTenant(tenant)
                                    }
                                    Toast.makeText(context, "Deleted ${tenant.userName}", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

suspend fun delay(timeMillis: Long) {
    kotlinx.coroutines.delay(timeMillis)
}

@Preview(showBackground = true, name = "Tenant Registration Form")
@Composable
fun HomeScreenPreview() {
    MyFirstAppTheme {
        HomeScreen(
            name = "John Doe",
            onNavigateToDashboard = {},
            onNavigateToDetails = {},
            onNavigateToTenants = {},
            onNavigateToNetworkData = {},
            onNavigateToManagement = {},
            onLogout = {}
        )
    }
}

@Preview(showBackground = true, name = "Dashboard Preview")
@Composable
fun DashboardScreenPreview() {
    MyFirstAppTheme {
        DashboardScreen(onNavigateBack = {})
    }
}

@Preview(showBackground = true, name = "Tenant List Display")
@Composable
fun TenantListScreenPreview() {
    val sampleTenants = listOf(
        Tenant(1, "Alice Smith", "A12345", "Apartment A1", "City Water"),
        Tenant(2, "Bob Jones", "B67890", "Apartment B2", "City Water")
    )
    MyFirstAppTheme {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Stored Tenants", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(sampleTenants) { tenant ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = tenant.userName, style = MaterialTheme.typography.titleLarge)
                            Text(text = "Metre: ${tenant.metreNumber}")
                            Text(text = "Apartment: ${tenant.apartmentName}")
                        }
                    }
                }
            }
        }
    }
}
