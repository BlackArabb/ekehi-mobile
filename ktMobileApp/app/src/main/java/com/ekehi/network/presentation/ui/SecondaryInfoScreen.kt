package com.ekehi.network.presentation.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import com.ekehi.network.domain.model.Country
import com.ekehi.network.ui.theme.EkehiMobileTheme
import com.ekehi.network.R
import com.ekehi.network.domain.model.CountryData
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.presentation.viewmodel.SecondaryInfoViewModel
import com.ekehi.network.presentation.ui.components.CountryDropdownField

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SecondaryInfoScreen(
    viewModel: SecondaryInfoViewModel = hiltViewModel(),
    onSecondaryInfoSuccess: () -> Unit
) {
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val countries by viewModel.countries.collectAsState()
    
    // Collect state properly
    val secondaryInfoState by viewModel.secondaryInfoState.collectAsState()
    
    // Derive UI states from the collected state
    val isLoading = secondaryInfoState is Resource.Loading
    val errorMessage = (secondaryInfoState as? Resource.Error)?.message

    // Handle secondary info success
    LaunchedEffect(secondaryInfoState) {
        Log.d("SecondaryInfoScreen", "Secondary info state changed: ${secondaryInfoState.javaClass.simpleName}")
        when (secondaryInfoState) {
            is Resource.Success -> {
                Log.d("SecondaryInfoScreen", "Secondary info submission successful, navigating to main screen")
                onSecondaryInfoSuccess()
            }
            is Resource.Error -> {
                // Use safe cast to avoid smart cast issues
                (secondaryInfoState as? Resource.Error)?.let { error ->
                    Log.e("SecondaryInfoScreen", "Secondary info submission failed: ${error.message}")
                }
                // Error is automatically displayed via errorMessage
            }
            is Resource.Loading -> {
                Log.d("SecondaryInfoScreen", "Secondary info submission in progress...")
            }
            is Resource.Idle -> {
                Log.d("SecondaryInfoScreen", "Secondary info state is idle")
            }
        }
    }

    // Reset the state when the screen is first displayed
    LaunchedEffect(Unit) {
        Log.d("SecondaryInfoScreen", "Screen mounted - resetting state")
        viewModel.resetState()
    }
    
    // Get focus manager and keyboard controller
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a1a2e),
                        Color(0xFF16213e),
                        Color(0xFF0f3460)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp), // Space for bottom button
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Content area with scrolling
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val scrollState = rememberScrollState()
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    // Ekehi Logo
                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher),
                        contentDescription = "Ekehi Logo",
                        modifier = Modifier
                            .size(80.dp)
                            .padding(bottom = 16.dp)
                    )

                    Text(
                        text = "Complete your registration",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "Please provide additional information to complete your registration",
                        color = Color(0xB3FFFFFF),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Warning about phone number
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0x33ffa000)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = Color(0xFFffa000),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Important: Your phone number is the only way to recover your password. Please ensure it is active and reachable.",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Country Selection Dropdown
                    CountryDropdownField(
                        countries = countries,
                        selectedCountry = selectedCountry,
                        onCountrySelected = { viewModel.onCountrySelected(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    // Phone Number Input
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { viewModel.onPhoneNumberChanged(it) },
                        label = { Text("Phone Number", color = Color(0xB3FFFFFF)) },
                        placeholder = { Text("e.g. +234 800 000 0000", color = Color(0x66FFFFFF)) },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFffa000),
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            cursorColor = Color(0xFFffa000),
                            focusedLabelColor = Color(0xFFffa000),
                            unfocusedLabelColor = Color(0xB3FFFFFF)
                        ),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                        )
                    )
                }
            }
        }

        // Bottom-anchored Submit Button for thumb-friendly access
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            // Submit Button
            Button(
                onClick = {
                    Log.d("SecondaryInfoScreen", "=== SUBMIT BUTTON CLICKED ===")
                    Log.d("SecondaryInfoScreen", "Phone Number: '$phoneNumber'")
                    Log.d("SecondaryInfoScreen", "Country: '${selectedCountry?.name}'")
                    Log.d("SecondaryInfoScreen", "isLoading: $isLoading")
                    
                    // Clear focus and hide keyboard
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    
                    val trimmedPhoneNumber = phoneNumber.trim()
                    val countryName = selectedCountry?.name ?: ""
                    
                    // Validate inputs
                    val isValid = trimmedPhoneNumber.isNotEmpty() && 
                                countryName.isNotEmpty()
                    
                    if (isValid) {
                        Log.d("SecondaryInfoScreen", "All inputs valid, proceeding with secondary info submission...")
                        viewModel.submitSecondaryInfo(trimmedPhoneNumber, countryName)
                    } else {
                        Log.d("SecondaryInfoScreen", "Validation failed:")
                        if (trimmedPhoneNumber.isEmpty()) Log.d("SecondaryInfoScreen", "- Phone number is empty")
                        if (countryName.isEmpty()) Log.d("SecondaryInfoScreen", "- Country is empty")
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFffa000),
                    disabledContainerColor = Color(0x33FFFFFF)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Continue",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Error Message
        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
            )
        }
    }
}
