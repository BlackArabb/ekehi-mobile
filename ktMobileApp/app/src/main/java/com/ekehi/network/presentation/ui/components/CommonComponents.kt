package com.ekehi.network.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ekehi.network.domain.model.Country

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryDropdownField(
    countries: List<Country>,
    selectedCountry: Country?,
    onCountrySelected: (Country) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(selectedCountry?.name ?: "") }
    
    // Update search query when selected country changes from outside
    LaunchedEffect(selectedCountry) {
        if (!expanded) {
            searchQuery = selectedCountry?.name ?: ""
        }
    }

    val filteredCountries = remember(searchQuery, countries, expanded) {
        if (!expanded && selectedCountry != null && searchQuery == selectedCountry.name) {
            countries
        } else if (searchQuery.isEmpty()) {
            countries
        } else {
            countries.filter { 
                it.name.contains(searchQuery, ignoreCase = true) || 
                it.code.contains(searchQuery) 
            }
        }
    }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { 
            expanded = it
            if (!it) {
                // When closing, reset search query to selected country name
                searchQuery = selectedCountry?.name ?: ""
            }
        },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it
                expanded = true 
            },
            label = { Text("Select Country", color = Color(0xB3FFFFFF)) },
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFffa000),
                unfocusedBorderColor = Color(0x33FFFFFF),
                cursorColor = Color(0xFFffa000),
                focusedLabelColor = Color(0xFFffa000),
                unfocusedLabelColor = Color(0xB3FFFFFF)
            ),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        )

        if (filteredCountries.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { 
                    expanded = false
                    searchQuery = selectedCountry?.name ?: ""
                },
                modifier = Modifier.background(Color(0xFF16213e))
            ) {
                filteredCountries.forEach { country ->
                    DropdownMenuItem(
                        text = {
                            Row {
                                Text(text = country.name, color = Color.White)
                                Spacer(modifier = Modifier.weight(1f))
                                Text(text = country.code, color = Color(0xB3FFFFFF))
                            }
                        },
                        onClick = {
                            onCountrySelected(country)
                            searchQuery = country.name
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
