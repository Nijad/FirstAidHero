package com.svu.firstaidhero;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.svu.firstaidhero.ui.MainActivity;

/**
 * كلاس التطبيق العام.
 * يثبّت معالجًا عامًا للأخطاء غير المتوقعة لمنع الإغلاق المفاجئ.
 */
public class App extends Application {

    private static final String TAG = "FirstAidHero";

    @Override
    public void onCreate() {
        super.onCreate();
        installCrashHandler();
    }

    private void installCrashHandler() {
        final Thread.UncaughtExceptionHandler defaultHandler =
                Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e(TAG, "Uncaught exception on thread: " + thread.getName(), throwable);

            try {
                // نعيد تشغيل النشاط الرئيسي بدل إغلاق التطبيق
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } catch (Exception ignored) {
                // إذا فشل حتى هذا، نعود للمعالج الافتراضي
                if (defaultHandler != null) {
                    defaultHandler.uncaughtException(thread, throwable);
                }
            } finally {
                // إنهاء العملية الحالية بعد إعادة التشغيل
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }
        });
    }
}