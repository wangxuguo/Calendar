# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/tangqifa/Develop/dev-tool/sdk/tools/proguard/proguard-android.txt
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






#galleryfinal
-keep class cn.finalteam.galleryfinal.widget.*{*;}
-keep class cn.finalteam.galleryfinal.widget.crop.*{*;}
-keep class cn.finalteam.galleryfinal.widget.zoonview.*{*;}
#不混淆R类里及其所有内部static类中的所有static变量字段
-keepclassmembers class **.R$* {
    public static <fields>;
}

#common
-dontwarn org.slf4j.**
-dontwarn com.squareup.picasso.**
-dontwarn com.parse.**
-dontwarn com.google.**
-dontwarn com.alibaba.**
-dontwarn cn.bingoogolapple.**
-dontwarn okio.**
-dontwarn android.support.**
-dontwarn com.alibaba.fastjson.**
-dontwarn com.squareup.okhttp.**
-dontwarn retrofit.**
-dontwarn android.support.**
# 保留了继承自Activity、Application这些类的子类
# 因为这些子类有可能被外部调用
# 比如第一行就保证了所有Activity的子类不要被混淆

-keep public class * extends android.app.Fragment
-keep public class * extends android.support.v4.**
-keep public class * extends android.support.annotation.**
-keep public class * extends android.support.v7.**
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class com.android.vending.licensing.ILicensingService

-keep class com.oceansky.teacher.** { *; }
#rxjava
-dontwarn rx.**
-keep class rx.** { *; }

-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
 long producerIndex;
 long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
 rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
 rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

-dontwarn com.hwangjr.rxbus.**
-keep class com.hwangjr.rxbus.** { *;}

-keepattributes *Annotation*
-keepclassmembers class ** {
    @com.hwangjr.rxbus.annotation.Subscribe public *;
    @com.hwangjr.rxbus.annotation.Produce public *;
}


 # 个推
 -dontwarn com.igexin.**
 -keep class com.igexin.**{*;}

 # 自动更新 ---------------------------------------------------
 -ignorewarnings
 -keepattributes Signature
 -keep class cn.bmob.v3.** {*;}
 -keep class cn.bmob.push.** {*;}

 # 保证继承自BmobObject、BmobUser类的JavaBean不被混淆
 -keep class * extends cn.bmob.v3.BmobObject {
     *;
 }
 -keep class com.example.bmobexample.bean.BankCard{*;}
 -keep class com.example.bmobexample.bean.GameScore{*;}
 -keep class com.example.bmobexample.bean.MyUser{*;}
 -keep class com.example.bmobexample.bean.Person{*;}
 -keep class com.example.bmobexample.file.Movie{*;}
 -keep class com.example.bmobexample.file.Song{*;}
 -keep class com.example.bmobexample.relation.Post{*;}
 -keep class com.example.bmobexample.relation.Comment{*;}

 # 如果你需要兼容6.0系统，请不要混淆org.apache.http.legacy.jar
  -dontwarn android.net.compatibility.**
  -dontwarn android.net.http.**
  -dontwarn com.android.internal.http.multipart.**
  -dontwarn org.apache.commons.**
  -dontwarn org.apache.http.**
  -keep class android.net.compatibility.**{*;}
  -keep class android.net.http.**{*;}
  -keep class com.android.internal.http.multipart.**{*;}
  -keep class org.apache.commons.**{*;}
  -keep class org.apache.http.**{*;}
  # ---------------------------------------------------

  #友盟统计
  -keepclassmembers class * {
      public <init> (org.json.JSONObject);
  }
  -keep public class com.oceansky.R$*{
      public static final int *;
  }
  -keepclassmembers enum * {
      public static **[] values();
      public static ** valueOf(java.lang.String);
  }
#event-bus
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

# Only required if you use AsyncExecutor
-keepclassmembers class * extends de.greenrobot.event.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}
#letv
-keep class com.letv.** { *;}
-keep class com.lecloud.** {*;}
-keep class android.webkit.** { *;}
-dontwarn com.avdmg.avdsmart.**
-dontwarn com.lecloud.**
-dontwarn com.letv.adlib.**
-dontwarn com.letv.play.**
-dontwarn com.letv.skin.**
-keep class com.letv.skin.**
-dontwarn com.letv.pp.**
-dontwarn org.rajawali3d.**
-dontwarn android.webkit.**
-dontwarn com.letv.skin.widget.**
-keep class com.letv.skin.widget.**
-dontwarn com.letv.universal.widget.**


-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keepattributes *Annotation*
-keep class org.springframework.web..** { *;}
-dontwarn org.springframework.web..**
#processing.AbstractProcessor

-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}
-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

-keep class com.squareup.okhttp.** { *; }
-keep class com.alibaba.fastjson.** { *;}
-keep class com.facebook.**
-keep class retrofit.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}
-keep class com.google.gson.** { *; }
-dontwarn com.squareup.okhttp3.**
-keep class com.squareup.okhttp3.** { *;}
-keep interface com.squareup.okhttp3.** { *;}
-dontwarn okio.**

-keep class org.joda.time.** { *; }
-dontwarn org.joda.time.**