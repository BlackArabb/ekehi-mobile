package com.ekehi.network.di

import com.ekehi.network.service.AppwriteService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppwriteServiceEntryPoint {
    fun appwriteService(): AppwriteService
}