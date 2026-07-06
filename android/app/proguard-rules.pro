# Add project specific ProGuard rules here.
-keepattributes Signature
-keepattributes *Annotation*

# Retrofit
-keep class retrofit2.** { *; }
-keep interface com.medrem.app.data.remote.api.** { *; }

# Gson
-keep class com.medrem.app.data.remote.dto.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }
