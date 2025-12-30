package com.ekehi.network.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.ekehi.network.data.model.AdContent
import com.ekehi.network.data.model.AdType
import com.ekehi.network.domain.model.Resource

@Composable
fun AdsCarousel(
    adsResource: Resource<List<AdContent>>,
    currentAdIndex: Int,
    onAdIndexChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    when (adsResource) {
        is Resource.Loading -> {
            // Show loading indicator
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFFffa000),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        is Resource.Error -> {
            // Show error message
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x1AFFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Failed to load ads",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
        is Resource.Success -> {
            val ads = adsResource.data
            if (ads.isNotEmpty()) {
                AdsCarouselContent(
                    ads = ads,
                    currentAdIndex = currentAdIndex,
                    onAdIndexChanged = onAdIndexChanged,
                    modifier = modifier
                )
            } else {
                // Show empty state
                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x1AFFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No ads available",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
        is Resource.Idle -> {
            // Show empty state
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x1AFFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Loading ads...",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun AdsCarouselContent(
    ads: List<AdContent>,
    currentAdIndex: Int,
    onAdIndexChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Ad content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            ads.getOrNull(currentAdIndex)?.let { ad ->
                AdItem(
                    ad = ad,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            // Handle ad click - could open URL or trigger action
                        }
                )
            }
        }

        // Dots indicator
        if (ads.size > 1) {
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(ads.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (index == currentAdIndex) Color(0xFFffa000) else Color(0x80FFFFFF)
                            )
                            .padding(2.dp)
                    )
                    
                    if (index < ads.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
            
            // Previous/Next buttons
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        val prevIndex = if (currentAdIndex == 0) ads.size - 1 else currentAdIndex - 1
                        onAdIndexChanged(prevIndex)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8b5cf6)
                    ),
                    modifier = Modifier
                        .size(40.dp)
                        .aspectRatio(1f)
                ) {
                    Text(
                        text = "<",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = "${currentAdIndex + 1} / ${ads.size}",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                
                Button(
                    onClick = {
                        val nextIndex = (currentAdIndex + 1) % ads.size
                        onAdIndexChanged(nextIndex)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8b5cf6)
                    ),
                    modifier = Modifier
                        .size(40.dp)
                        .aspectRatio(1f)
                ) {
                    Text(
                        text = ">",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AdItem(
    ad: AdContent,
    modifier: Modifier = Modifier
) {
    when (ad.type) {
        AdType.IMAGE, AdType.ANIMATED_IMAGE -> {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(ad.content)
                        .build()
                ),
                contentDescription = ad.title,
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .fillMaxSize()
                    .background(Color(0x1AFFFFFF))
            )
        }
        AdType.TEXT -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x1AFFFFFF))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = ad.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = ad.content,
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}