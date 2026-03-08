# ─── Crashlytics: preserve stack traces ───────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keep public class * extends java.lang.Exception

# ─── Firebase / GMS ───────────────────────────────────────────────────────────
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ─── Room ─────────────────────────────────────────────────────────────────────
# Entities: fields read via reflection during migrations & schema export
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.TypeConverter class * { *; }
-keep class * extends androidx.room.RoomDatabase { *; }

# ─── WorkManager (HiltWorkerFactory instantiates via reflection) ───────────────
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ─── kotlinx.serialization ────────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
    <fields>;
}
-keep class **$$serializer { *; }

# ─── Kotlin Coroutines ────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# ─── Coil 3 ───────────────────────────────────────────────────────────────────
-dontwarn coil3.**

# ─── Google Credential Manager / GoogleId ─────────────────────────────────────
-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn androidx.credentials.**

# ─── OkHttp / Retrofit ────────────────────────────────────────────────────────
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-keepclassmembernames interface * {
    @retrofit2.http.* <methods>;
}
