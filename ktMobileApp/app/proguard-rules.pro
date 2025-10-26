# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces:
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name:
#-renamesourcefileattribute SourceFile

# Appwrite SDK
-keep class io.appwrite.** { *; }
-dontwarn io.appwrite.**

# Hilt/Dagger
-keep class dagger.**
-keep class javax.inject.**
-keep class javax.annotation.**
-keep class kotlin.**
-keepclasseswithmembers class * {
    @dagger.* <methods>;
}
-keepclasseswithmembers class * {
    @javax.inject.* <init>(...);
}
-dontwarn dagger.**
-dontwarn javax.inject.**
-dontwarn kotlin.**

# Room
-keep class androidx.room.** { *; }
-keep class androidx.room.paging.** { *; }
-keep @androidx.room.Entity class *
-keep @androidx.room.Database class *
-dontwarn androidx.room.paging.**

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Coil
-keep class coil.** { *; }
-dontwarn coil.**

# Start.io
-keep class com.startapp.** { *; }
-dontwarn com.startapp.**

# Gson
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# Jetpack Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ViewModel
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

# Navigation
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

# Chrome Custom Tabs
-keep class androidx.browser.** { *; }
-dontwarn androidx.browser.**

# Keep data classes
-keep class com.ekehi.network.data.model.** { *; }
-keep class com.ekehi.network.domain.model.** { *; }

# Keep repository classes
-keep class com.ekehi.network.data.repository.** { *; }

# Keep use case classes
-keep class com.ekehi.network.domain.usecase.** { *; }

# Keep view model classes
-keep class com.ekehi.network.presentation.viewmodel.** { *; }

# Keep service classes
-keep class com.ekehi.network.network.service.** { *; }

# Keep analytics classes
-keep class com.ekehi.network.analytics.** { *; }

# Keep performance classes
-keep class com.ekehi.network.performance.** { *; }

# Keep sync classes
-keep class com.ekehi.network.data.sync.** { *; }

# Keep local data classes
-keep class com.ekehi.network.data.local.** { *; }

# Keep DI classes
-keep class com.ekehi.network.di.** { *; }