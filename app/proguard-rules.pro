# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep Gson model classes from obfuscation
-keep class com.Jnx03.thaiflickkeyboard.model.FlickKey { *; }
-keep class com.Jnx03.thaiflickkeyboard.model.KeyboardLayout { *; }

# Gson specific rules
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
