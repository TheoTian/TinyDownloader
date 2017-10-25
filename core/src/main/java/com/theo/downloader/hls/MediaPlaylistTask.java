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

import com.theo.downloader.Task;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * MediaPlaylistTask
 *
 * @author: theotian
 * @since: 17/10/25
 * @describe:
 */
public class MediaPlaylistTask {

    /**
     * save directory
     */
    private String savePath;

    /**
     * task list waiting to download
     */
    private Queue<Task> waitingList = new LinkedList<>();
    /**
     * downloading task list
     */
    private LinkedList<Task> downloadList = new LinkedList<>();
    /**
     * complete task list
     */
    private LinkedList<Task> completeList = new LinkedList<>();

    /**
     * all complete
     */
    private boolean allComplete = false;

    /**
     * complete total size
     */
    private long completeSize = 0;

    public MediaPlaylistTask(MediaPlaylist mediaPlaylist, String savePath) {
        this.savePath = savePath;
        inflateWaitingTaskList(mediaPlaylist);
    }

    /**
     * inflate the task
     */
    private void inflateWaitingTaskList(MediaPlaylist mediaPlaylist) {
        if (mediaPlaylist == null || mediaPlaylist.getList().size() <= 0) {
            return;
        }
        /**
         * first inflate waiting task
         */
        for (MediaPlaylist.Segment segment : mediaPlaylist.getList()) {
            waitingList.add(new Task(segment.getUri(), segment.getUri(), savePath));
        }
    }

    /**
     * pop task from waiting queue and throw to download list
     *
     * @return
     */
    public Task popTaskToDownload() {
        Task headTask = waitingList.poll();
        if (headTask != null) {
            downloadList.add(headTask);
        }
        return headTask;
    }

    /**
     * throw task to complete list
     *
     * @param task
     * @return
     */
    public boolean throwTaskToComplete(Task task) {
        if (task == null || !downloadList.contains(task)) {
            return false;
        }
        downloadList.remove(task);
        completeList.add(task);
        completeSize += task.getDownSize();
        if (waitingList.size() <= 0 && downloadList.size() <= 0) {
            allComplete = true;
        }
        return true;
    }

    public long getCompleteSize() {
        return completeSize;
    }

    public MediaPlaylistTask setCompleteSize(long completeSize) {
        this.completeSize = completeSize;
        return this;
    }

    /**
     * is all complete
     *
     * @return
     */
    public boolean allComplete() {
        return allComplete;
    }

    /**
     * has Download Task
     *
     * @return
     */
    public boolean hasDownloadTask() {
        return downloadList.size() > 0;
    }

    /**
     * get download list
     *
     * @return
     */
    public List<Task> getCompelteList() {
        return completeList;
    }

}
