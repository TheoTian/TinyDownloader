/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2017, theotian
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.theo.downloader;


import com.theo.downloader.info.SnifferInfo;

import java.nio.ByteBuffer;

public interface IDownloader {

    byte TYPE_NORMAL_DOWNLOADER = 0;
    byte TYPE_MULTI_SEGMENT_DOWNLOADER = 1;
    byte TYPE_HLS_DOWNLOADER = 2;

    int OK = 0;
    int ERROR = -1;
    int ERROR_ALREADY_CREATE = -2;

    int create();

    int load(ByteBuffer data);

    int start();

    int pause();

    int delete();

    interface DownloadListener {

        void onCreated(Task task, SnifferInfo snifferInfo);

        void onStart(Task task);

        void onPause(Task task);

        void onProgress(Task task, long total, long down);

        void onError(Task task, int error, String msg);

        void onComplete(Task task, long total);

        /**
         * callback resume data
         * this will call after paused.
         * you can use the data to load and continue download from paused position.
         *
         * @param task
         * @param data resume data
         */
        void onSaveInstance(Task task, byte[] data);
    }

    void setListener(DownloadListener l);
}
