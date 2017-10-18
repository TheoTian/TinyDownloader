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
import com.theo.downloader.util.NumberUtil;
import com.theo.downloader.util.UrlUtil;

public class CreateRunner implements Runnable {

    private AbstractDownloader downloader;

    public CreateRunner(AbstractDownloader downloader) {
        this.downloader = downloader;
    }

    /**
     * start sniffer
     */
    @Override
    public void run() {
        if (downloader != null) {
            Task task = downloader.getTask();
            sniffer(task);
        }
    }

    /**
     * 下载任务之前进行302探测
     *
     * @param task
     * @return
     */
    private boolean sniffer(Task task) {
        if (task == null) {
            downloader.cbOnError(IDownloader.ERROR, "task is null");
            return false;
        }
        SnifferInfo snifferInfo = DownloaderUtil.sniffer(task.getUrl());
        if (snifferInfo == null) {
            downloader.cbOnError(IDownloader.ERROR, "sniffer failed");
            return false;
        }

        downloader.cbOnCreated(snifferInfo);
        task.setTotalSize(NumberUtil.parseInt(snifferInfo.contentLength));
        task.setRealUrl(snifferInfo.realUrl);
        task.setFileName(UrlUtil.getFileName(snifferInfo.realUrl));
        return true;
    }
}
