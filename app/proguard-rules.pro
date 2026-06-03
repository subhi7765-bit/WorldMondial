# ProGuard keep rules for sa.mondial.world - Production Release Configuration

# Optimization & General Settings
-dontobfuscate
-dontoptimize
-keepattributes EnclosingMethod,InnerClasses,Signature,*Annotation*,SourceFile,LineNumberTable

# Kotlin Coroutines and Standard Library Keep Rules
-keep class kotlin.coroutines.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**
-dontwarn kotlin.**

# Room Database Production Rules (DAOs, Entities, Databases)
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Entity class * { *; }
-keep class * { @androidx.room.Database *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}
-dontwarn androidx.room.**

# Retrofit, OkHttp and Okio Release Rules
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Kotlinx Serialization Rules (@Serializable, companion descriptor, SerialName)
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <init>(...);
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName *;
}
-keep class * implements kotlinx.serialization.KSerializer {
    *;
}

# Gson / general SerializedName and Annotation Preservation
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Common Parcelable and java.io.Serializable Rules
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Firebase Services (FCM, Analytics, and Crashlytics) Production Rules
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Mondial Firebase Messaging Service Specific Keep Rules
-keep class sa.mondial.world.core.notifications.MondialFirebaseMessagingService { *; }
-keepclassmembers class sa.mondial.world.core.notifications.MondialFirebaseMessagingService {
    *;
}

# Keep All Classes in sa.mondial.world.core.notifications.*
-keep class sa.mondial.world.core.notifications.** { *; }

# Coil Image Loader Release Rules
-keep class coil.** { *; }
-dontwarn coil.**

# Hilt Dependencies Injection Keep Rules
-keep class *__HiltComponents* { *; }
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.EntryPoint class * { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}

# Core Network DTOs Keep Rules
-keep class sa.mondial.world.core.network.dto.** { *; }
-keep class sa.mondial.world.core.network.** { *; }

# Core Database Entities Keep Rules
-keep class sa.mondial.world.core.database.entity.** { *; }
-keep class sa.mondial.world.core.database.** { *; }

# Core Domain Models Keep Rules
-keep class sa.mondial.world.core.domain.** { *; }