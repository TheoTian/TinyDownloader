package com.theo.downloader.util;

import java.lang.ref.WeakReference;

/**
 * Simple Messager
 *
 * @author: theotian
 * @since: 17/10/27
 * <p>
 * Copyright © 17/10/27 MGTV. All rights reserved.
 */

public class Messager {

    public interface MessageListener {
        void onMessage(String msg);
    }

    private static WeakReference<MessageListener> messageHandler;//in case of mem leak

    /**
     * bind the handler
     *
     * @param handler
     */
    public static void bindMessageHandler(MessageListener handler) {
        messageHandler = new WeakReference<MessageListener>(handler);
    }

    /**
     * out valid after bindMessageHandler
     *
     * @param msg
     */
    public static void out(String msg) {
        MessageListener handler;
        if (messageHandler != null && (handler = messageHandler.get()) != null) {
            handler.onMessage(msg);
            return;
        }
        System.out.println(msg);
    }
}
