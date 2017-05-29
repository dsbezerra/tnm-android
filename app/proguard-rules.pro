# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Diego\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}


# Fix SearchView crash with proguard
-keep class android.support.v7.widget.SearchView { *; }

# Realm Database Lib
-keep class io.realm.annotations.RealmModule
-keep @io.realm.annotations.RealmModule class *
-keep class io.realm.internal.Keep
-keep @io.realm.internal.Keep class * { *; }
-dontwarn javax.**
-dontwarn io.realm.**

# Retrofit 2.X
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Remove warnings okhttp/okio
-dontwarn com.squareup.okhttp.**
-dontwarn okio.**

# MUPDF
-keep class com.tnmlicitacoes.app.mupdf.** {*;}

# Remove all logging methods
-assumenosideeffects class com.tnmlicitacoes.app.utils.LogUtils {
    public static *** LOG_DEBUG(...);
}

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** d(...);
    public static *** e(...);
}