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
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun ImageAdsCarousel(
    imageAdsResource: Resource<List<AdContent>>,
    modifier: Modifier = Modifier
) {
    // Image Ads Carousel only
    AdsCarousel(
        adsResource = imageAdsResource,
        title = "Image Ads",
        adTypeFilter = listOf(AdType.IMAGE, AdType.ANIMATED_IMAGE),
        modifier = modifier
    )
}

@Composable
fun AdsCarousel(
    adsResource: Resource<List<AdContent>>,
    title: String,
    adTypeFilter: List<AdType>,
    modifier: Modifier = Modifier
) {
    // Add logging at the start
    LaunchedEffect(adsResource) {
        println("==============================")
        println("📺 $title - Resource State: ${adsResource.javaClass.simpleName}")
        when (adsResource) {
            is Resource.Success -> {
            println("✅ Total ads in resource: ${adsResource.data.size}")
            adsResource.data.forEachIndexed { index, ad ->
                println("  Ad $index: ID=${ad.id}, Type=${ad.type}")
            }
        }
        is Resource.Error -> println("❌ Error: ${(adsResource as Resource.Error).message}")
        is Resource.Loading -> println("⏳ Loading...")
        is Resource.Idle -> println("💤 Idle")
    }
    println("==============================")
}
    
    when (adsResource) {
        is Resource.Loading -> {
            Box(
                modifier = modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = Color(0xFFffa000),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Loading $title...",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
        is Resource.Error -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x1AFFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Failed to load $title",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
        is Resource.Success -> {
            val ads = adsResource.data.filter { ad -> ad.type in adTypeFilter }
            
            // Add logging after filtering
            LaunchedEffect(ads) {
                println("🔍 $title - After filtering: ${ads.size} ads")
                ads.forEachIndexed { index, ad ->
                    println("  Filtered Ad $index: ID=${ad.id}, Type=${ad.type}")
                }
            }
            
            if (ads.isNotEmpty()) {
                ImageAdsCarouselContent(
                    ads = ads,
                    title = title,
                    modifier = modifier
                )
            } else {
                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x1AFFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No $title available",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
        is Resource.Idle -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x1AFFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Loading $title...",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun ImageAdsCarouselContent(
    ads: List<AdContent>,
    title: String,
    modifier: Modifier = Modifier
) {
    var currentAdIndex by remember { mutableIntStateOf(0) }
    var startX by remember { mutableFloatStateOf(0f) }
    
    val safeIndex = currentAdIndex.coerceIn(0, ads.size - 1)
    
    // Add comprehensive logging
    LaunchedEffect(ads.size) {
        println("================================")
        println("📊 $title")
        println("Total ads: ${ads.size}")
        println("Show navigation: ${ads.size > 1}")
        println("Current index: $currentAdIndex")
        println("Safe index: $safeIndex")
        ads.forEachIndexed { index, ad ->
            println("  Ad $index: ID=${ad.id}, Title=${ad.title}")
        }
        println("================================")
    }
    
    LaunchedEffect(currentAdIndex) {
        println("🔄 $title - Index changed to: $currentAdIndex (safe: $safeIndex)")
    }
    
    Column(modifier = modifier) {
        // Title
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        // Ad content with swipe support
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            startX = offset.x
                        },
                        onDragEnd = {
                            startX = 0f
                        },
                        onDrag = { change, _ ->
                            val endX = change.position.x
                            val diffX = endX - startX
                            
                            // Require significant drag distance (at least 100 pixels)
                            if (kotlin.math.abs(diffX) > 100) {
                                if (diffX > 0) {
                                    // Swiped right - go to previous
                                    currentAdIndex = if (safeIndex > 0) {
                                        safeIndex - 1
                                    } else {
                                        ads.size - 1
                                    }
                                } else {
                                    // Swiped left - go to next
                                    currentAdIndex = if (safeIndex < ads.size - 1) {
                                        safeIndex + 1
                                    } else {
                                        0
                                    }
                                }
                                // Reset start position to prevent multiple triggers
                                startX = endX
                            }
                        }
                    )
                }
        ) {
            // Display current ad
            ads.getOrNull(safeIndex)?.let { ad ->
                key(ad.id) { // Use ad.id as key to force recomposition
                    AdItem(
                        ad = ad,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Dots indicator and controls
        if (ads.size > 1) {
            LaunchedEffect(Unit) {
                println("✅ Showing dots and buttons (ads.size = ${ads.size})")
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(ads.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == safeIndex) 10.dp else 8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (index == safeIndex) Color(0xFFffa000) else Color(0x80FFFFFF)
                            )
                            .clickable {
                                currentAdIndex = index
                            }
                    )
                    
                    if (index < ads.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
            
            // Previous/Next buttons with counter
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous button
                Button(
                    onClick = {
                        currentAdIndex = if (safeIndex == 0) {
                            ads.size - 1
                        } else {
                            safeIndex - 1
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8b5cf6)
                    ),
                    modifier = Modifier.size(40.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "‹",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Counter
                Text(
                    text = "${safeIndex + 1} / ${ads.size}",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                // Next button
                Button(
                    onClick = {
                        currentAdIndex = (safeIndex + 1) % ads.size
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8b5cf6)
                    ),
                    modifier = Modifier.size(40.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "›",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            LaunchedEffect(Unit) {
                println("❌ NOT showing dots and buttons (ads.size = ${ads.size})")
            }
        }
    }
}

@Composable
private fun AdItem(
    ad: AdContent,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Enhanced logging
    LaunchedEffect(ad.id) {
        println("========================================")
        println("Rendering Ad:")
        println("  ID: ${ad.id}")
        println("  Type: ${ad.type}")
        println("  Title: ${ad.title}")
        println("  Content: ${ad.content}")
        println("========================================")
    }
    
    when (ad.type) {
        AdType.IMAGE, AdType.ANIMATED_IMAGE -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color(0x1AFFFFFF))
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(context)
                            .data(ad.content)
                            .crossfade(true)
                            .listener(
                                onStart = {
                                    println("🔄 [${ad.title}] Image loading started...")
                                },
                                onError = { _, result ->
                                    println("❌ [${ad.title}] Image load error:")
                                    println("   Message: ${result.throwable.message}")
                                    println("   URL: ${ad.content}")
                                    result.throwable.printStackTrace()
                                },
                                onSuccess = { _, _ ->
                                    println("✅ [${ad.title}] Image loaded successfully!")
                                }
                            )
                            .build()
                    ),
                    contentDescription = ad.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            if (ad.actionUrl.isNotEmpty()) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ad.actionUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    println("Error opening URL: ${e.message}")
                                }
                            }
                        }
                )
                
                // Debug overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "ID: ${ad.id}",
                        color = Color.White,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "Title: ${ad.title}",
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }
        }
        else -> {
            // Handle TEXT and any other ad types by not displaying them
            Box(modifier = modifier) {
                // Empty box for unsupported ad types
            }
        }
    }
}