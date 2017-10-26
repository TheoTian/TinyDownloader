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


import com.theo.downloader.util.ByteUtil;
import com.theo.downloader.util.FileUtil;
import com.theo.downloader.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.nio.ByteBuffer;

class MultiThreadDownloader extends AbstractDownloader {

    /**
     * 分段
     */
    public static class Segment implements Serializable {
        public enum Status {
            DOWNLOADING, PAUSE, COMPLETE
        }

        public long start;//段开始位置
        public long end;//段结束位置
        public long down;//段已下载位置
        public Status status = Status.DOWNLOADING;

        public boolean isComplete() {
            return status == Status.COMPLETE;
        }

        public boolean isPaused() {
            return status == Status.PAUSE;
        }
    }

    public static class Segments implements Serializable {
        private Segment[] segmentArray;

        public Segments(int size) {
            segmentArray = new Segment[size];
        }

        public Segment[] getSegmentArray() {
            return segmentArray;
        }

        public Segments setSegmentArray(Segment[] segmentArray) {
            this.segmentArray = segmentArray;
            return this;
        }
    }

    /**
     * 默认分为5段
     */
    private int segmentNum = 5;
    private Segments segments;
    private Thread[] threads;

    /**
     * Segment Runner
     */
    public static class SegmentRunner implements Runnable {

        private String url;
        private Segment segment;
        private int index;//segments index
        private File dstFile;
        private MultiThreadDownloader downloader;

        public SegmentRunner(MultiThreadDownloader downloader, int index, File dstFile) {
            this.downloader = downloader;
            this.url = downloader.task.getRealUrl();
            this.index = index;
            this.segment = downloader.segments.segmentArray[index];
            this.dstFile = dstFile;
        }

        @Override
        public void run() {
            if (segment.isComplete()) {
                return;//this segment already complete
            }
            segment.status = Segment.Status.DOWNLOADING;

            RandomAccessFile randomAccessFile;
            HttpURLConnection connection = null;
            try {
                connection = DownloaderUtil.createConnection(url);
                if (connection == null) {
                    downloader.cbOnError(ERROR, "segment [" + index + "] create connection failed");
                    return;
                }

                connection.setRequestProperty("range", "bytes=" + segment.down + "-" + segment.end);
                System.out.println("bytes=" + segment.down + "-" + segment.end);

                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK
                        || connection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {

                    String valueAR = connection.getHeaderField("Accept-Ranges");
                    String valueCR = connection.getHeaderField("Content-Range");
                    /**
                     * if server not support range
                     * skip the data until target position
                     */
                    if ((valueAR == null || valueAR.trim().equalsIgnoreCase("") || valueAR.equalsIgnoreCase("none"))
                            && (valueCR == null || valueCR.trim().equalsIgnoreCase("") || valueCR.equalsIgnoreCase("none"))) { //not support range
                        //cbOnError
                        downloader.cbOnError(ERROR, "segment [" + index + "] not support range");
                        return;
                    }

                    InputStream is = connection.getInputStream();
                    byte[] buffer = new byte[BUFFER_SIZE];

                    randomAccessFile = new RandomAccessFile(dstFile, "rw");
                    randomAccessFile.seek(segment.down);

                    int read;
                    while (downloader.task.getTargetStatus() == Task.Status.DOWNLOADING
                            && (read = is.read(buffer)) > 0) {
                        randomAccessFile.write(buffer, 0, read);
                        segment.down = segment.down + read;
                    }

                    IOUtil.safeClose(randomAccessFile);
                    IOUtil.safeClose(is);

                    if (downloader.task.getTargetStatus() == Task.Status.PAUSE) {
                        segment.status = Segment.Status.PAUSE;//段暂停下载
                        return;
                    }

                    segment.status = Segment.Status.COMPLETE;//段下载完成

                    System.out.println("segment[" + index + "] download complete\n");
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }

    public MultiThreadDownloader() {
        super();
    }

    public MultiThreadDownloader(Task task) {
        super(task);
    }

    @Override
    protected void download(Task task) {
        long start = System.currentTimeMillis();

        String filePath = task.getDstDir() + "/" + task.getFileName();
        task.setFilePath(filePath);

        File dstFile = new File(filePath);
        if (!FileUtil.checkAndCreateFile(dstFile)) {
            cbOnError(ERROR, "create file failed");
            return;
        }

        if (task.getTotalSize() < 10 * 1024) {//small file no need more segment
            segmentNum = 1;
        }

        if(!dstFile.exists()) {
            segments = null;
        }

        threads = new Thread[segmentNum];

        if(segments == null) {
            FileUtil.delete(dstFile);
            segments = new Segments(segmentNum);
            fillSegments(segments.segmentArray, task.getTotalSize());
        }

        for (int i = 0; i < segmentNum; i++) {
            threads[i] = new Thread(new SegmentRunner(this, i, dstFile));
            threads[i].start();//开启子线程下载
        }

        checkProgress();

        System.out.println("download all complete\n cost:" + (System.currentTimeMillis() - start));
    }

    @Override
    protected byte getFlag() {
        return IDownloader.TYPE_MULTI_SEGMENT_DOWNLOADER;
    }

    @Override
    public int load(ByteBuffer data) {
        super.load(data);
        byte[] segsBytes = new byte[data.getInt()];
        data.get(segsBytes);
        segments = (Segments) ByteUtil.readObjectFromBytes(segsBytes);
        return OK;
    }

    /**
     * extends the save instance data
     *
     * @param os
     */
    @Override
    protected void writeExInstance(OutputStream os) throws IOException {
        byte[] segBytes = ByteUtil.writeObjectToBytes(segments);
        os.write(ByteUtil.cast(segBytes.length));
        os.write(segBytes);
    }

    /**
     * 检查进度
     * 在下载线程执行
     */
    private void checkProgress() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int completeCounter = 0;
            int pauseCounter = 0;//完成的或者被暂停的
            int down = 0;//全部下载数

            for (Segment segment : segments.segmentArray) {
                if (segment.isComplete()) {
                    completeCounter++;
                    pauseCounter ++;
                } else if(segment.isPaused()) {
                    pauseCounter ++;
                }
                down += (segment.down - segment.start);
            }

            cbOnProgress(task.getTotalSize(), down);
            /**
             * 全部完成
             */
            if (completeCounter >= segments.segmentArray.length) {
                cbOnComplete(task.getTotalSize());
                return;
            }

            /**
             * 全部暂停并且没有全部完成
             */
            if (pauseCounter >= segments.segmentArray.length) {
                task.setDownSize(down);
                cbOnPause();//pause
                return;
            }
        }
    }

    /**
     * 填充segment
     *
     * @param segments
     * @param total
     */
    private void fillSegments(Segment[] segments, long total) {
        long aver = total / segments.length;
        long remain = total - (aver * segments.length);

        for (int i = 0; i < segments.length; i++) {
            segments[i] = new Segment();
            segments[i].start = i * aver;
            segments[i].down = segments[i].start;
            segments[i].end = segments[i].start + (aver - 1);
        }

        segments[segments.length - 1].end = segments[segments.length - 1].end + remain;//last segment num add the remain
    }

}
