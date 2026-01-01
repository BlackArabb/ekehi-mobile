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

    private val _textAds = MutableStateFlow<Resource<List<AdContent>>>(Resource.Loading)
    val textAds: StateFlow<Resource<List<AdContent>>> = _textAds

    init {
        loadAds()
    }

    fun loadAds() {
        viewModelScope.launch {
            _imageAds.value = Resource.Loading
            _textAds.value = Resource.Loading
            
            try {
                Log.d("AdsViewModel", "================================")
                Log.d("AdsViewModel", "🔄 Loading ads from repository...")
                
                val images = adsRepository.getImageAds()
                val texts = adsRepository.getTextAds()
                
                Log.d("AdsViewModel", "✅ Loaded ${images.size} image ads")
                images.forEachIndexed { index, ad ->
                    Log.d("AdsViewModel", "  Image Ad $index: ${ad.title}")
                }
                
                Log.d("AdsViewModel", "✅ Loaded ${texts.size} text ads")
                texts.forEachIndexed { index, ad ->
                    Log.d("AdsViewModel", "  Text Ad $index: ${ad.title}")
                }
                Log.d("AdsViewModel", "================================")
                
                _imageAds.value = Resource.Success(images)
                _textAds.value = Resource.Success(texts)
            } catch (e: Exception) {
                Log.e("AdsViewModel", "❌ Failed to load ads", e)
                _imageAds.value = Resource.Error(e.message ?: "Failed to load ads")
                _textAds.value = Resource.Error(e.message ?: "Failed to load ads")
            }
        }
    }
}