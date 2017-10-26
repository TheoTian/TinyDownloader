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

import com.theo.downloader.hls.HLSDownloader;

import java.nio.ByteBuffer;

public class DownloaderFactory {

    public enum Type {
        NORMAL, MULTI_SEGMENT, HLS
    }

    /**
     * create downloader
     *
     * @param type NORMAL MULTI_SEGMENT HLS
     * @param task download task
     * @return downloader instance
     */
    public static IDownloader create(Type type, Task task) {
        switch (type) {
            case NORMAL:
                return new NormalDownloader(task);
            case MULTI_SEGMENT:
                return new MultiSegmentDownloader(task);
            case HLS:
                return new HLSDownloader(task);
        }
        return new NormalDownloader(task);
    }

    /**
     * load downloader
     *
     * @param data 第一个字节为下载器类型标识 0 NormalDownloader
     * @return load downloader instance
     */
    public static IDownloader load(byte[] data) {
        if (data == null || data.length <= 0) {
            return null;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        byte type = byteBuffer.get();
        IDownloader downloader = null;
        if (type == IDownloader.TYPE_NORMAL_DOWNLOADER) {
            downloader = new NormalDownloader();
        } else if (type == IDownloader.TYPE_MULTI_SEGMENT_DOWNLOADER) {
            downloader = new MultiSegmentDownloader();
        }
        downloader.load(byteBuffer);
        return downloader;
    }

}
