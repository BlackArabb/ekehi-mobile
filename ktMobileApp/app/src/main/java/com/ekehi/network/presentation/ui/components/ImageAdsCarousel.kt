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
import android.util.Log
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ImageAdsCarousel(
    imageAdsResource: Resource<List<AdContent>>,
    modifier: Modifier = Modifier
) {
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
    LaunchedEffect(adsResource) {
        Log.d("AdsCarousel", "==============================")
        Log.d("AdsCarousel", "📺 $title - Resource State: ${adsResource.javaClass.simpleName}")
        when (adsResource) {
            is Resource.Success -> {
                Log.d("AdsCarousel", "✅ Total ads in resource: ${adsResource.data.size}")
                adsResource.data.forEachIndexed { index, ad ->
                    Log.d("AdsCarousel", "  Ad $index: ID=${ad.id}, Type=${ad.type}")
                }
            }
            is Resource.Error -> Log.e("AdsCarousel", "❌ Error: ${adsResource.message}")
            is Resource.Loading -> Log.d("AdsCarousel", "⏳ Loading...")
            is Resource.Idle -> Log.d("AdsCarousel", "💤 Idle")
        }
        Log.d("AdsCarousel", "==============================")
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
            
            LaunchedEffect(ads) {
                Log.d("AdsCarousel", "🔍 $title - After filtering: ${ads.size} ads")
                ads.forEachIndexed { index, ad ->
                    Log.d("AdsCarousel", "  Filtered Ad $index: ID=${ad.id}, Type=${ad.type}")
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
    var isUserInteracting by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val safeIndex = currentAdIndex.coerceIn(0, ads.size - 1)
    
    // Add comprehensive logging
    LaunchedEffect(ads.size) {
        Log.d("AdsCarouselContent", "================================")
        Log.d("AdsCarouselContent", "📊 $title")
        Log.d("AdsCarouselContent", "Total ads: ${ads.size}")
        Log.d("AdsCarouselContent", "Show navigation: ${ads.size > 1}")
        Log.d("AdsCarouselContent", "Current index: $currentAdIndex")
        Log.d("AdsCarouselContent", "Safe index: $safeIndex")
        ads.forEachIndexed { index, ad ->
            Log.d("AdsCarouselContent", "  Ad $index: ID=${ad.id}, Title=${ad.title}")
        }
        Log.d("AdsCarouselContent", "================================")
    }
    
    LaunchedEffect(currentAdIndex) {
        Log.d("AdsCarouselContent", "🔄 $title - Index changed to: $currentAdIndex (safe: $safeIndex)")
    }
    
    // Auto-rotation effect - rotates every 8 seconds
    LaunchedEffect(ads.size, isUserInteracting) {
        if (ads.size > 1 && !isUserInteracting) {
            while (true) {
                kotlinx.coroutines.delay(8000) // Wait 8 seconds
                currentAdIndex = (currentAdIndex + 1) % ads.size
            }
        }
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
        
        // Ad content with swipe support and smooth cover transition
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight() // Use fillMaxHeight instead of weight to respect parent height
                .clip(RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isUserInteracting = true
                            startX = offset.x
                        },
                        onDragEnd = {
                            startX = 0f
                            // Delay before resuming auto-rotation
                            scope.launch {
                                kotlinx.coroutines.delay(8000)
                                isUserInteracting = false
                            }
                        },
                        onDrag = { change, _ ->
                            val endX = change.position.x
                            val diffX = endX - startX
                            
                            if (kotlin.math.abs(diffX) > 100) {
                                if (diffX > 0) {
                                    currentAdIndex = if (safeIndex > 0) {
                                        safeIndex - 1
                                    } else {
                                        ads.size - 1
                                    }
                                } else {
                                    currentAdIndex = if (safeIndex < ads.size - 1) {
                                        safeIndex + 1
                                    } else {
                                        0
                                    }
                                }
                                startX = endX
                                
                                // Mark as user interaction and delay auto-rotation
                                isUserInteracting = true
                                scope.launch {
                                    kotlinx.coroutines.delay(8000)
                                    isUserInteracting = false
                                }
                            }
                        }
                    )
                }
                .clickable {
                    // Mark as user interaction when clicked
                    isUserInteracting = true
                    scope.launch {
                        kotlinx.coroutines.delay(8000)
                        isUserInteracting = false
                    }
                }
        ) {
            // Smooth cover transition animation
            AnimatedContent(
                targetState = safeIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        // Next: slide in from right
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(durationMillis = 1000) // 1 second transition
                        ) + fadeIn(
                            animationSpec = tween(durationMillis = 1000)
                        ) togetherWith slideOutHorizontally(
                            targetOffsetX = { fullWidth -> -fullWidth },
                            animationSpec = tween(durationMillis = 1000)
                        ) + fadeOut(
                            animationSpec = tween(durationMillis = 1000)
                        )
                    } else {
                        // Previous: slide in from left
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> -fullWidth },
                            animationSpec = tween(durationMillis = 1000)
                        ) + fadeIn(
                            animationSpec = tween(durationMillis = 1000)
                        ) togetherWith slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(durationMillis = 1000)
                        ) + fadeOut(
                            animationSpec = tween(durationMillis = 1000)
                        )
                    }
                },
                label = "AdCoverTransition",
                modifier = Modifier.fillMaxSize()
            ) { targetIndex ->
                ads.getOrNull(targetIndex)?.let { ad ->
                    key(ad.id) {
                        AdItem(
                            ad = ad,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // Dots indicator and controls
        if (ads.size > 1) {
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
                                // Mark as user interaction when dot is clicked
                                currentAdIndex = index
                                isUserInteracting = true
                                scope.launch {
                                    kotlinx.coroutines.delay(8000)
                                    isUserInteracting = false
                                }
                            }
                    )
                    
                    if (index < ads.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
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
    val context = LocalContext.current
    
    LaunchedEffect(ad.id) {
        Log.d("AdItem", "========================================")
        Log.d("AdItem", "Rendering Ad:")
        Log.d("AdItem", "  ID: ${ad.id}")
        Log.d("AdItem", "  Type: ${ad.type}")
        Log.d("AdItem", "  Title: ${ad.title}")
        Log.d("AdItem", "  Content: ${ad.content}")
        Log.d("AdItem", "========================================")
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
                                    Log.d("AdItem", "🔄 [${ad.title}] Image loading started...")
                                },
                                onError = { _, result ->
                                    Log.e("AdItem", "❌ [${ad.title}] Image load error:")
                                    Log.e("AdItem", "   Message: ${result.throwable.message}")
                                    Log.e("AdItem", "   URL: ${ad.content}")
                                    result.throwable.printStackTrace()
                                },
                                onSuccess = { _, _ ->
                                    Log.d("AdItem", "✅ [${ad.title}] Image loaded successfully!")
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
                                    Log.e("AdItem", "Error opening URL: ${e.message}")
                                }
                            }
                        }
                )
                
                // Ads indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .background(Color(0xFF8b5cf6))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Ads",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        else -> {
            Box(modifier = modifier) {
                // Empty for unsupported ad types
            }
        }
    }
}

