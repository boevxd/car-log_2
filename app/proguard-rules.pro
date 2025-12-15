-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**
-dontwarn org.jetbrains.annotations.**
-keepattributes *Annotation*

-keep class dm.com.carlog.CarLogApp { *; }
-keep class dm.com.carlog.MainActivity { *; }

-keep @androidx.compose.runtime.Composable class * { *; }
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

-keep class dagger.hilt.** { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponent { *; }
-dontwarn dagger.hilt.internal.**
-dontwarn javax.inject.**
-dontwarn dagger.**

-keep class androidx.room.** { *; }
-keep interface androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**

-keep class dm.com.carlog.data.** { *; }

-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

-keep class coil.** { *; }
-dontwarn coil.**

-keep class dm.com.carlog.util.** { *; }
-dontwarn dm.com.carlog.util.**

-keep class dm.com.carlog.model.**ViewModel { *; }

-keep class dm.com.carlog.di.AppModule { *; }

-keepattributes SourceFile,LineNumberTable