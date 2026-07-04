# Add project specific ProGuard rules here.
# Control optimization and shrinking profile explicitly.

# 1. Attributes & Source files configuration
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod
-renamesourcefileattribute SourceFile

# 2. General Kotlin support rules
-dontwarn kotlin.**
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations

# 3. Kotlinx Serialization DTO Keeping
# Prevents shrinking or renaming of all @Serializable models (User, Project, Pitch, Transaction) 
# and their synthetic Companion serializers to avoid reflection failures.
-keep class com.example.data.model.** { *; }
-keepclassmembers class com.example.data.model.** { *; }

-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keepclassmembers class **$Serializer {
    *;
}
-keepclassmembers class ** {
    *** Companion;
}
-keepclassmembers class ** {
    *** Companion$*;
}

# 4. Local Room Database Security & Structural Keeps
# Keeps generated database schemas, internal mappings, and Daos perfectly unified.
-keep class * extends androidx.room.RoomDatabase
-keep class com.example.data.local.** { *; }
-keepclassmembers class com.example.data.local.** { *; }
-dontwarn androidx.room.**

# 5. Network & Networking Utilities
# Preserve OkHttp & Ktor client configurations from static reverse-engineering and shrinking anomalies.
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }
