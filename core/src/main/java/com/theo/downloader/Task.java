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


import com.theo.downloader.util.HashUtil;

import java.io.Serializable;

public class Task implements Serializable{

    /**
     * 任务状态
     */
    public enum Status {
        NONE, CREATE, DOWNLOADING, PAUSE, ERROR, COMPLETE
    }

    private Status currentStatus = Status.NONE;//任务状态
    private Status targetStatus = Status.NONE;

    private String key;//任务标识,默认使用url整个为KEY
    private String url;//任务源
    private String realUrl;//最终下载源，可能302跳转

    //文件目录将为dstDir/dstFile
    private String dstDir;//保存目标目录
    private String fileName;//保存目标文件
    private String filePath;//file absolute path


    private long downSize;//已经下载bytes
    private long downSpeed;//byte/s

    public Task(String url, String dstDir) {
        this.url = url;
        this.key = HashUtil.MD5Encrypt(url);
        this.dstDir = dstDir;
    }

    public Task(String key, String url, String dstDir) {
        this.key = key;
        this.url = url;
        this.dstDir = dstDir;
    }

    /**
     * 最终需要下载的bytes，从200的content-length中获取。
     * 可能服务器传回有误，下载完成后再更新一次，已实际更新为准
     */
    private long totalSize;

    public Status getCurrentStatus() {
        return currentStatus;
    }

    public Task setCurrentStatus(Status currentStatus) {
        this.currentStatus = currentStatus;
        return this;
    }

    public Status getTargetStatus() {
        return targetStatus;
    }

    public Task setTargetStatus(Status targetStatus) {
        this.targetStatus = targetStatus;
        return this;
    }

    /**
     * update target status & current status
     *
     * @param status
     * @return
     */
    public Task updateStatus(Status status) {
        this.targetStatus = status;
        this.currentStatus = status;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public Task setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getDstDir() {
        return dstDir;
    }

    public Task setDstDir(String dstDir) {
        this.dstDir = dstDir;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public Task setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getRealUrl() {
        return realUrl;
    }

    public Task setRealUrl(String realUrl) {
        this.realUrl = realUrl;
        return this;
    }

    public long getDownSize() {
        return downSize;
    }

    public Task setDownSize(long downSize) {
        this.downSize = downSize;
        return this;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public Task setTotalSize(long totalSize) {
        this.totalSize = totalSize;
        return this;
    }

    public long getDownSpeed() {
        return downSpeed;
    }

    public Task setDownSpeed(long downSpeed) {
        this.downSpeed = downSpeed;
        return this;
    }

    public String getFilePath() {
        return filePath;
    }

    public Task setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public boolean isDownloading() {
        return currentStatus == Status.DOWNLOADING;
    }
}
