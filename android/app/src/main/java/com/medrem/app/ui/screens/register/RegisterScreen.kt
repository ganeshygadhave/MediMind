package com.medrem.app.ui.screens.register

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.medrem.app.ui.theme.*

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

    Box(modifier = Modifier.fillMaxSize()) {
        // Top gradient header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.28f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(PrimaryTeal, PrimaryDark)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(top = 48.dp),
            ) {
                // Back button
                IconButton(
                    onClick = {
                        if (uiState.currentStep > 1) viewModel.previousStep()
                        else onNavigateBack()
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Join MediMind to manage your health",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f),
                )
            }
        }

        // Card overlay
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 200.dp, bottom = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
            ) {
                // Step indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    StepIndicator(step = 1, currentStep = uiState.currentStep, label = "Account")
                    
                    // Connector line
                    Box(
                        modifier = Modifier
                            .padding(top = 18.dp)
                            .width(48.dp)
                            .height(2.dp)
                            .background(
                                if (uiState.currentStep >= 2) PrimaryTeal else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(1.dp),
                            )
                    )
                    
                    StepIndicator(step = 2, currentStep = uiState.currentStep, label = "Health")
                }

                Spacer(modifier = Modifier.height(28.dp))

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
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    ) {
                        Text(
                            text = uiState.error ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                if (uiState.currentStep == 1) {
                    Button(
                        onClick = viewModel::nextStep,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    ) {
                        Text("Continue", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
                    }
                } else {
                    Button(
                        onClick = viewModel::register,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        enabled = !uiState.isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Create Account", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Login link
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Already have an account?", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = onNavigateBack) {
                        Text("Sign In", fontWeight = FontWeight.SemiBold, color = PrimaryTeal)
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
                .clip(CircleShape)
                .background(if (isActive) PrimaryTeal else MaterialTheme.colorScheme.outlineVariant),
            contentAlignment = Alignment.Center,
        ) {
            if (currentStep > step) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Text("$step", color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = if (isActive) PrimaryTeal else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
private fun AccountInfoStep(uiState: RegisterUiState, viewModel: RegisterViewModel) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = PrimaryTeal,
        focusedLabelColor = PrimaryTeal,
        cursorColor = PrimaryTeal,
    )

    Column {
        Text("Account Information", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PrimaryDark)
        Text("Set up your login credentials", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 24.dp))

        OutlinedTextField(
            value = uiState.fullName,
            onValueChange = { viewModel.onFieldChange("fullName", it) },
            label = { Text("Full Name *") },
            leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = PrimaryTeal) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            colors = fieldColors,
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = { viewModel.onFieldChange("email", it) },
            label = { Text("Email *") },
            leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = PrimaryTeal) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            singleLine = true,
            colors = fieldColors,
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = uiState.phone,
            onValueChange = { viewModel.onFieldChange("phone", it) },
            label = { Text("Mobile Number *") },
            placeholder = { Text("e.g. 9876543210") },
            leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = PrimaryTeal) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
            singleLine = true,
            colors = fieldColors,
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.onFieldChange("password", it) },
            label = { Text("Password *") },
            leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = PrimaryTeal) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            singleLine = true,
            colors = fieldColors,
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = uiState.confirmPassword,
            onValueChange = { viewModel.onFieldChange("confirmPassword", it) },
            label = { Text("Confirm Password *") },
            leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = PrimaryTeal) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            singleLine = true,
            colors = fieldColors,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HealthProfileStep(uiState: RegisterUiState, viewModel: RegisterViewModel) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = PrimaryTeal,
        focusedLabelColor = PrimaryTeal,
        cursorColor = PrimaryTeal,
    )

    Column {
        Text("Health Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PrimaryDark)
        Text("Complete your medical information", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 24.dp))

        OutlinedTextField(
            value = uiState.dateOfBirth, 
            onValueChange = { 
                if (it.length <= 8) viewModel.onFieldChange("dateOfBirth", it.filter { char -> char.isDigit() }) 
            }, 
            label = { Text("Date of Birth *") }, 
            placeholder = { Text("YYYYMMDD") }, 
            leadingIcon = { Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = PrimaryTeal) }, 
            modifier = Modifier.fillMaxWidth(), 
            shape = RoundedCornerShape(14.dp), 
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = DateVisualTransformation(),
            singleLine = true,
            colors = fieldColors,
        )

        Spacer(modifier = Modifier.height(14.dp))

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
                leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = PrimaryTeal) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors,
            )
            ExposedDropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                genderOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = { viewModel.onFieldChange("gender", option); genderExpanded = false }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

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
                leadingIcon = { Icon(imageVector = Icons.Default.Bloodtype, contentDescription = null, tint = PrimaryTeal) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bloodExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors,
            )
            ExposedDropdownMenu(expanded = bloodExpanded, onDismissRequest = { bloodExpanded = false }) {
                bloodOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = { viewModel.onFieldChange("bloodType", option); bloodExpanded = false }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = uiState.allergies,
            onValueChange = { viewModel.onFieldChange("allergies", it) },
            label = { Text("Allergies") },
            placeholder = { Text("e.g. Penicillin, Peanuts") },
            leadingIcon = { Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = PrimaryTeal) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors,
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = uiState.medicalConditions,
            onValueChange = { viewModel.onFieldChange("medicalConditions", it) },
            label = { Text("Medical Conditions") },
            placeholder = { Text("e.g. Hypertension, Diabetes") },
            leadingIcon = { Icon(imageVector = Icons.Default.LocalHospital, contentDescription = null, tint = PrimaryTeal) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors,
        )
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
