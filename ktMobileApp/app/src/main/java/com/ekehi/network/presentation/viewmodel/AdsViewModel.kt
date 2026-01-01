package com.ekehi.network.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekehi.network.data.model.AdContent
import com.ekehi.network.data.repository.AdsRepository
import com.ekehi.network.domain.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdsViewModel @Inject constructor(
    private val adsRepository: AdsRepository
) : ViewModel() {
    
    private val _imageAds = MutableStateFlow<Resource<List<AdContent>>>(Resource.Loading)
    val imageAds: StateFlow<Resource<List<AdContent>>> = _imageAds

    init {
        loadImageAds()
    }

    fun loadImageAds() {
        viewModelScope.launch {
            _imageAds.value = Resource.Loading
            
            try {
                Log.d("AdsViewModel", "================================")
                Log.d("AdsViewModel", "🔄 Loading image ads from repository...")
                
                val images = adsRepository.getImageAds()
                
                Log.d("AdsViewModel", "✅ Loaded ${images.size} image ads")
                images.forEachIndexed { index, ad ->
                    Log.d("AdsViewModel", "  Image Ad $index: ${ad.title}")
                }
                Log.d("AdsViewModel", "================================")
                
                _imageAds.value = Resource.Success(images)
            } catch (e: Exception) {
                Log.e("AdsViewModel", "❌ Failed to load image ads", e)
                _imageAds.value = Resource.Error(e.message ?: "Failed to load image ads")
            }
        }
    }
}