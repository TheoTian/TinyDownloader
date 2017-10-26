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
import com.theo.downloader.util.ByteUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public abstract class AbstractDownloader implements IDownloader {

    protected final static int BUFFER_SIZE = 8 * 1024;

    protected Task task;
    protected DownloadListener listener;

    protected Thread createThread;
    protected Thread downThread;

    protected CreateRunner createRunner;
    protected DownloadRunner downloadRunner;

    protected DownSpeedCalculator calculator;

    public AbstractDownloader() {
        this.downloadRunner = new DownloadRunner(this);
        this.createRunner = new CreateRunner(this);
    }

    public AbstractDownloader(Task task) {
        this();
        this.task = task;
        calculator = new DownSpeedCalculator(task);
    }

    @Override
    public int create() {
        if (task.getTargetStatus() != Task.Status.NONE) {
            return ERROR_ALREADY_CREATE;
        }
        task.setTargetStatus(Task.Status.CREATE);
        createThread = new Thread(createRunner);
        createThread.start();
        return OK;
    }

    @Override
    public int load(ByteBuffer data) {
        int taskObjSize = data.getInt();
        byte[] taskBytes = new byte[taskObjSize];
        data.get(taskBytes);
        task = (Task) ByteUtil.readObjectFromBytes(taskBytes);
        calculator = new DownSpeedCalculator(task);
        return OK;
    }

    @Override
    public int start() {
        if (task.getCurrentStatus() != Task.Status.CREATE
                && task.getCurrentStatus() != Task.Status.PAUSE) {
            return ERROR;
        }
        task.setTargetStatus(Task.Status.DOWNLOADING);
        downThread = new Thread(downloadRunner);
        downThread.start();
        return OK;
    }

    @Override
    public int pause() {
        if (task.getCurrentStatus() != Task.Status.DOWNLOADING) {
            return ERROR;
        }
        task.setTargetStatus(Task.Status.PAUSE);
        return OK;
    }

    @Override
    public int delete() {
        task.updateStatus(Task.Status.NONE);
        return OK;
    }

    @Override
    public void setListener(DownloadListener l) {
        listener = l;
    }

    /**
     * must be thread safe
     * <p>
     * running in the thread
     *
     * @param task task need to download
     */
    protected abstract void download(Task task);

    protected abstract byte getFlag();

    public Task getTask() {
        return task;
    }

    public void cbOnCreated(SnifferInfo snifferInfo) {
        task.updateStatus(Task.Status.CREATE);
        if (listener != null) {
            listener.onCreated(task, snifferInfo);
        }
    }

    /**
     * when error or complete not callback any event
     *
     * @return is error or complete status
     */
    public boolean isErrorOrComplete() {
        return task.getCurrentStatus() == Task.Status.ERROR
                || task.getCurrentStatus() == Task.Status.COMPLETE;
    }

    public void cbOnStart() {
        if (isErrorOrComplete()) {
            return;
        }
        task.updateStatus(Task.Status.DOWNLOADING);
        if (calculator != null) {
            calculator.startCalculateDownSpeed();
        }
        if (listener != null) {
            listener.onStart(task);
        }
    }

    public void cbOnPause() {
        if (isErrorOrComplete()) {
            return;
        }
        task.updateStatus(Task.Status.PAUSE);
        if (listener != null) {
            listener.onPause(task);
        }

        saveInstance();
    }

    /**
     * save instance
     */
    private void saveInstance() {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            os.write(getFlag());
            byte[] taskBytes = ByteUtil.writeObjectToBytes(task);
            int size = taskBytes.length;
            os.write(ByteUtil.cast(size));
            os.write(taskBytes);
            writeExInstance(os);
            os.flush();
            cbOnSaveInstance(os.toByteArray());
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract void writeExInstance(OutputStream os) throws IOException;

    public void cbOnProgress(long total, long down) {
        if (isErrorOrComplete()) {
            return;
        }
        task.setDownSize(down);
        if (calculator != null) {
            calculator.endCalculateDownSpeed();
            calculator.startCalculateDownSpeed();
        }
        if (listener != null) {
            listener.onProgress(task, total, down);
        }
    }

    public void cbOnError(int error, String msg) {
        if (isErrorOrComplete()) {
            return;
        }
        task.updateStatus(Task.Status.ERROR);
        if (listener != null) {
            listener.onError(task, error, msg);
        }
    }

    public void cbOnComplete(long total) {
        if (isErrorOrComplete()) {
            return;
        }
        task.updateStatus(Task.Status.COMPLETE);
        if (listener != null) {
            listener.onComplete(task, total);
        }
    }

    public void cbOnSaveInstance(byte[] data) {
        if (isErrorOrComplete()) {
            return;
        }
        if (listener != null) {
            listener.onSaveInstance(task, data);
        }
    }
}
