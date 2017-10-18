package com.theo.demo.downloader;

import android.os.Handler;
import android.os.Looper;

/**
 * Author: theotian
 * Date: 17/10/17
 * Describe:
 */

public class RunnableUtil {

    /**
     * run action on ui thread
     * @param runnable
     */
    public static void runOnUIThread(Runnable runnable) {
        Looper mainLooper = Looper.getMainLooper();
        if (Thread.currentThread() == mainLooper.getThread()) {//already main thread
            runnable.run();
        } else {
            new Handler(mainLooper).post(runnable);
        }
    }
}
