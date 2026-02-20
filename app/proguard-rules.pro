# Keep Firestore DTOs (important)
-keep class com.arnoagape.polyscribe.data.dto.** { *; }

# Keep no-arg constructors (Firestore needs them)
-keepclassmembers class com.arnoagape.polyscribe.data.dto.** {
    public <init>();
}

# Keep annotations (Firestore uses them)
-keepattributes *Annotation*