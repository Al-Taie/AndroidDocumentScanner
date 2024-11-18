-keepattributes *Annotation*

# Gson uses generic type information stored in a class file when working with
# collections. Proguard removes such information by default, so configure it
# to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

-keep class com.scanner.library.NativeScanner { *; }
-keep class com.scanner.library.DocumentScanner { *; }
-keep class com.scanner.library.common.** { *; }

-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}
-dontwarn com.scanner.library.DocumentScanner
-dontwarn com.scanner.library.common.CameraViewKt
-dontwarn com.scanner.library.common.GraphicOverlayKt

