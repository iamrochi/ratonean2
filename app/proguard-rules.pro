# P1.5: reglas R8 para release (isMinifyEnabled=true).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Room ---
-keep class androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-dontwarn androidx.room.paging.**

# --- kotlinx-serialization (DTOs y Entities) ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *; }
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable *;
}
# Modelos serializados por nombre (no ofuscar fields usados por backend/Room)
-keep class com.example.ratonean2_app.** {
    <fields>;
    <init>(...);
}

# --- Ktor ---
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# --- Coil ---
-keep class coil.** { *; }
-dontwarn coil.**

# --- MapLibre / Maps ---
-keep class org.maplibre.** { *; }
-dontwarn org.maplibre.**
-keep class com.google.android.gms.maps.** { *; }

# --- Koin (resolución por reflexión en viewModels) ---
-keep class org.koin.** { *; }

# --- Solo tests (no van en release, silenciar R8) ---
-dontwarn org.apiguardian.**
-dontwarn org.junit.**
-dontwarn org.opentest4j.**
