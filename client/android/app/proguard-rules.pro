-dontobfuscate
-keep public class com.fasterxml.jackson.module.kotlin.*
-dontwarn javax.annotation.*

# Serializer for classes with named companion objects are retrieved using `getDeclaredClasses`.
# If you have any, replace classes with those containing named companion objects.
-keepattributes InnerClasses # Needed for `getDeclaredClasses`.

-if @kotlinx.serialization.Serializable class
com.almgru.prilla.android.net.cookie.SerializableHttpCookie,
com.almgru.prilla.android.model.Entry,
java.time.LocalDateTime
{
    static **$* *;
}
-keepnames class <1>$$serializer { # -keepnames suffices; class is kept when serializer() is kept.
    static <1>$$serializer INSTANCE;
}