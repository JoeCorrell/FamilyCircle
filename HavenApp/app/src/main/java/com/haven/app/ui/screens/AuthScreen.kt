package com.haven.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haven.app.ui.theme.LocalHavenColors
import com.haven.app.ui.theme.OutfitFamily
import com.haven.app.ui.theme.SpaceMonoFamily

@Composable
fun AuthScreen(
    isLoading: Boolean,
    error: String?,
    onSignIn: (phone: String, password: String) -> Unit,
    onSignUp: (phone: String, password: String) -> Unit,
    onClearError: () -> Unit
) {
    val t = LocalHavenColors.current
    val focusManager = LocalFocusManager.current
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(t.bg)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.height(40.dp))

        // Logo
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(t.accent, t.accentMid))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "H", fontSize = 36.sp, fontWeight = FontWeight.Black,
                color = Color.White, fontFamily = OutfitFamily
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "Haven", fontSize = 28.sp, fontWeight = FontWeight.Black,
            color = t.text, fontFamily = OutfitFamily, letterSpacing = (-1).sp
        )
        Text(
            "Family Safety Reimagined", fontSize = 12.sp,
            color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.sp
        )

        Spacer(Modifier.height(36.dp))

        // Phone Number
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it; onClearError() },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Phone Number") },
            placeholder = { Text("+1 (555) 123-4567") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            colors = authFieldColors(t)
        )

        Spacer(Modifier.height(12.dp))

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; onClearError() },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = if (isSignUp) ImeAction.Next else ImeAction.Done
            ),
            keyboardActions = if (!isSignUp) KeyboardActions(onDone = {
                focusManager.clearFocus()
                onSignIn(phone, password)
            }) else KeyboardActions.Default,
            colors = authFieldColors(t)
        )

        // Confirm password (sign up only)
        AnimatedVisibility(visible = isSignUp) {
            Column {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; onClearError() },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Confirm Password") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        if (password == confirmPassword) onSignUp(phone, password)
                    }),
                    colors = authFieldColors(t)
                )
            }
        }

        // Error
        AnimatedVisibility(visible = error != null) {
            Text(
                error ?: "", fontSize = 12.sp, color = t.danger,
                fontFamily = OutfitFamily,
                modifier = Modifier.padding(top = 12.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(24.dp))

        // Submit button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(listOf(t.accent, t.accentMid)))
                .clickable(enabled = !isLoading) {
                    focusManager.clearFocus()
                    if (isSignUp) {
                        if (password == confirmPassword && phone.isNotBlank() && password.length >= 6) {
                            onSignUp(phone.trim(), password)
                        }
                    } else {
                        if (phone.isNotBlank() && password.isNotBlank()) {
                            onSignIn(phone.trim(), password)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text(
                    if (isSignUp) "Create Account" else "Sign In",
                    fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = Color.White, fontFamily = OutfitFamily
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Toggle sign in / sign up
        Row(horizontalArrangement = Arrangement.Center) {
            Text(
                if (isSignUp) "Already have an account? " else "Don't have an account? ",
                fontSize = 13.sp, color = t.textMid, fontFamily = OutfitFamily
            )
            Text(
                if (isSignUp) "Sign In" else "Sign Up",
                fontSize = 13.sp, fontWeight = FontWeight.Bold,
                color = t.accent, fontFamily = OutfitFamily,
                modifier = Modifier.clickable {
                    isSignUp = !isSignUp
                    onClearError()
                }
            )
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun authFieldColors(t: com.haven.app.ui.theme.HavenColors) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = t.accent,
    unfocusedBorderColor = t.border,
    focusedContainerColor = if (t.isDark) t.surfaceAlt else t.bgSub,
    unfocusedContainerColor = if (t.isDark) t.surfaceAlt else t.bgSub,
    cursorColor = t.accent,
    focusedTextColor = t.text,
    unfocusedTextColor = t.text,
    focusedLabelColor = t.accent,
    unfocusedLabelColor = t.textFade,
)
