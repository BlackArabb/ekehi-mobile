package com.ekehi.network.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ekehi.network.R

@Composable
fun EkhLogo(
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    Image(
        painter = painterResource(id = R.mipmap.ic_launcher),
        contentDescription = "EKH Logo",
        modifier = modifier.size(size)
    )
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun SectionText(text: String) {
    Text(
        text = text,
        color = Color(0xB3FFFFFF),
        fontSize = 16.sp,
        lineHeight = 24.sp
    )
}