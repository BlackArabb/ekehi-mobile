package com.ekehi.network.di

import com.ekehi.network.service.StartIoService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface StartIoServiceEntryPoint {
    fun startIoService(): StartIoService
}