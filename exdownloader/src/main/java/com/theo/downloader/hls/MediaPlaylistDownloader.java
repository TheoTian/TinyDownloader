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

package com.theo.downloader.hls;

import com.theo.downloader.DownloaderFactory;
import com.theo.downloader.IDownloader;
import com.theo.downloader.Task;
import com.theo.downloader.info.SnifferInfo;

/**
 * MediaPlaylistDownloader
 *
 * @author: theotian
 * @since: 17/10/25
 */
public class MediaPlaylistDownloader {

    private HLSDownloader hlsDownloader;
    private MediaPlaylistTask taskList;
    private IDownloader downloader;
    private IDownloader.DownloadListener mediaSegmentListener;//sub segment listener

    private boolean autoStart = true;
    private boolean paused = false;

    IDownloader.DownloadListener listener = new IDownloader.DownloadListener() {
        @Override
        public void onCreated(Task task, SnifferInfo snifferInfo) {
            System.out.println("MediaPlaylistDownloader onCreated realUrl:" + snifferInfo.realUrl);
            System.out.println("MediaPlaylistDownloader onCreated contentLength:" + snifferInfo.contentLength);
            if (mediaSegmentListener != null) {
                mediaSegmentListener.onCreated(task, snifferInfo);
            }
            if (paused) {
                if (hlsDownloader != null) {
                    hlsDownloader.cbOnPause();
                    return;
                }
            }
            if (autoStart && downloader != null) {
                downloader.start();
            }
        }

        @Override
        public void onStart(Task task) {
            if (mediaSegmentListener != null) {
                mediaSegmentListener.onStart(task);
            }
            System.out.println("MediaPlaylistDownloader onStart");
        }

        @Override
        public void onPause(Task task) {
            if (mediaSegmentListener != null) {
                mediaSegmentListener.onPause(task);
            }
            if (hlsDownloader != null) {
                hlsDownloader.cbOnPause();
            }
            System.out.println("MediaPlaylistDownloader onPause");
        }

        @Override
        public void onProgress(Task task, long total, long down) {
            if (mediaSegmentListener != null) {
                mediaSegmentListener.onProgress(task, total, down);
            }
        }

        @Override
        public void onError(Task task, int error, String msg) {
            System.out.println("MediaPlaylistDownloader onError [" + error + "," + msg + "]");
            if (mediaSegmentListener != null) {
                mediaSegmentListener.onError(task, error, msg);
            }
            if (hlsDownloader != null) {
                hlsDownloader.cbOnError(error, msg);
            }
        }

        @Override
        public void onComplete(Task task, long total) {
            System.out.println("MediaPlaylistDownloader onComplete [" + total + "]");
            if (mediaSegmentListener != null) {
                mediaSegmentListener.onComplete(task, total);
            }
            taskList.throwTaskToComplete(task);
            if (taskList.allComplete()) {
                System.out.println("MediaPlaylistDownloader all task onComplete [" + total + "]");
                if (hlsDownloader != null) {
                    hlsDownloader.updateM3U8URI(taskList);
                    hlsDownloader.cbOnComplete(taskList.getCompleteSize());
                }
            } else {
                pop2Download();
            }
        }

        @Override
        public void onSaveInstance(Task task, byte[] data) {
            System.out.println("MediaPlaylistDownloader onSaveInstance data.length:" + data.length);
        }

    };

    public MediaPlaylistDownloader(HLSDownloader hlsDownloader, MediaPlaylistTask taskList) {
        this.hlsDownloader = hlsDownloader;
        this.taskList = taskList;
    }

    public void download() {
        paused = false;
        if (downloader != null) {
            downloader.start();
        } else {
            pop2Download();
        }
    }

    public void pause() {
        if (downloader != null) {
            downloader.pause();
            paused = true;
        } else {
            paused = true;
        }
    }

    /**
     * download next task
     */
    private void pop2Download() {
        if (taskList == null) {
            return;
        }

        Task task = taskList.popTaskToDownload();
        if (task == null) {
            return;
        }

        try {
            downloader = DownloaderFactory.create(DownloaderFactory.Type.NORMAL, task);
            downloader.setListener(listener);
            downloader.create();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMediaSegmentListener(IDownloader.DownloadListener mediaSegmentListener) {
        this.mediaSegmentListener = mediaSegmentListener;
    }

}
