package com.medrem.app.ui.screens.register

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.medrem.app.ui.theme.PrimaryTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onRegisterSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.currentStep > 1) viewModel.previousStep()
                        else onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        ) {
            // Step indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                StepIndicator(step = 1, currentStep = uiState.currentStep, label = "Account Info")
                Spacer(modifier = Modifier.width(16.dp))
                StepIndicator(step = 2, currentStep = uiState.currentStep, label = "Health Profile")
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    slideInHorizontally { width -> if (targetState > initialState) width else -width } togetherWith
                    slideOutHorizontally { width -> if (targetState > initialState) -width else width }
                },
                label = "step_transition",
            ) { step ->
                when (step) {
                    1 -> AccountInfoStep(uiState, viewModel)
                    2 -> HealthProfileStep(uiState, viewModel)
                }
            }

            // Error
            AnimatedVisibility(visible = uiState.error != null) {
                Text(
                    text = uiState.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            if (uiState.currentStep == 1) {
                Button(
                    onClick = viewModel::nextStep,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                ) {
                    Text("Continue", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }
            } else {
                Button(
                    onClick = viewModel::register,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    } else {
                        Text("Create Account", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(step: Int, currentStep: Int, label: String) {
    val isActive = currentStep >= step
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    if (isActive) PrimaryTeal else MaterialTheme.colorScheme.outlineVariant,
                    RoundedCornerShape(50),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (currentStep > step) {
                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
            } else {
                Text("$step", color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = if (isActive) PrimaryTeal else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AccountInfoStep(uiState: RegisterUiState, viewModel: RegisterViewModel) {
    Column {
        Text("Account Information", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text("Create your login credentials", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 24.dp))

        OutlinedTextField(value = uiState.fullName, onValueChange = { viewModel.onFieldChange("fullName", it) }, label = { Text("Full Name *") }, leadingIcon = { Icon(Icons.Default.Person, null) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = uiState.email, onValueChange = { viewModel.onFieldChange("email", it) }, label = { Text("Email *") }, leadingIcon = { Icon(Icons.Default.Email, null) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = uiState.phone, onValueChange = { viewModel.onFieldChange("phone", it) }, label = { Text("Phone Number") }, leadingIcon = { Icon(Icons.Default.Phone, null) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = uiState.password, onValueChange = { viewModel.onFieldChange("password", it) }, label = { Text("Password *") }, leadingIcon = { Icon(Icons.Default.Lock, null) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), visualTransformation = PasswordVisualTransformation(), singleLine = true)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = uiState.confirmPassword, onValueChange = { viewModel.onFieldChange("confirmPassword", it) }, label = { Text("Confirm Password *") }, leadingIcon = { Icon(Icons.Default.Lock, null) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), visualTransformation = PasswordVisualTransformation(), singleLine = true)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HealthProfileStep(uiState: RegisterUiState, viewModel: RegisterViewModel) {
    Column {
        Text("Health Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text("Please complete your health profile", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 24.dp))

        OutlinedTextField(
            value = uiState.dateOfBirth, 
            onValueChange = { 
                if (it.length <= 8) viewModel.onFieldChange("dateOfBirth", it.filter { char -> char.isDigit() }) 
            }, 
            label = { Text("Date of Birth *") }, 
            placeholder = { Text("YYYY-MM-DD") }, 
            leadingIcon = { Icon(Icons.Default.CalendarToday, null) }, 
            modifier = Modifier.fillMaxWidth(), 
            shape = RoundedCornerShape(16.dp), 
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = DateVisualTransformation(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        var genderExpanded by remember { mutableStateOf(false) }
        val genderOptions = listOf("Male", "Female", "Others")
        ExposedDropdownMenuBox(
            expanded = genderExpanded,
            onExpandedChange = { genderExpanded = !genderExpanded }
        ) {
            OutlinedTextField(
                value = uiState.gender,
                onValueChange = {},
                readOnly = true,
                label = { Text("Gender *") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(16.dp)
            )
            ExposedDropdownMenu(
                expanded = genderExpanded,
                onDismissRequest = { genderExpanded = false }
            ) {
                genderOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            viewModel.onFieldChange("gender", option)
                            genderExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        var bloodExpanded by remember { mutableStateOf(false) }
        val bloodOptions = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        ExposedDropdownMenuBox(
            expanded = bloodExpanded,
            onExpandedChange = { bloodExpanded = !bloodExpanded }
        ) {
            OutlinedTextField(
                value = uiState.bloodType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Blood Type *") },
                leadingIcon = { Icon(Icons.Default.Bloodtype, null) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bloodExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(16.dp)
            )
            ExposedDropdownMenu(
                expanded = bloodExpanded,
                onDismissRequest = { bloodExpanded = false }
            ) {
                bloodOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            viewModel.onFieldChange("bloodType", option)
                            bloodExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = uiState.allergies, onValueChange = { viewModel.onFieldChange("allergies", it) }, label = { Text("Allergies") }, placeholder = { Text("Comma separated: Penicillin, Peanuts") }, leadingIcon = { Icon(Icons.Default.Warning, null) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = uiState.medicalConditions, onValueChange = { viewModel.onFieldChange("medicalConditions", it) }, label = { Text("Medical Conditions") }, placeholder = { Text("Comma separated: Hypertension, Diabetes") }, leadingIcon = { Icon(Icons.Default.LocalHospital, null) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
    }
}

class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 8) text.text.substring(0..7) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 3 || i == 5) out += "-"
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset <= 5) return offset + 1
                if (offset <= 8) return offset + 2
                return 10
            }
            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 4) return offset
                if (offset <= 7) return offset - 1
                if (offset <= 10) return offset - 2
                return 8
            }
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}
