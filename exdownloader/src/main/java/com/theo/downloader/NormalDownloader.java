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

import com.theo.downloader.info.ConnectionInfo;
import com.theo.downloader.util.FileUtil;
import com.theo.downloader.util.IOUtil;
import com.theo.downloader.util.Messager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

class NormalDownloader extends AbstractDownloader {

    public NormalDownloader() {
        super();
    }

    public NormalDownloader(Task task) {
        super(task);
    }

    @Override
    protected void download(Task task) {
        long start = System.currentTimeMillis();

        ConnectionInfo connectionInfo = null;
        try {
            connectionInfo = DownloaderUtil.createConnectionInfo(task.getRealUrl());
            if (connectionInfo == null) {
                cbOnError(ERROR, "create connection failed");
                return;
            }

            /**
             * already sniffer the url
             * no need to check HTTP_MOVED_TEMP again
             */
            HttpURLConnection connection = connectionInfo.getConnection();

            String filePath = task.getDstDir() + "/" + task.getFileName();
            task.setFilePath(filePath);
            
            File outFile = new File(filePath);

            long downSize = 0;
            boolean append = false;

            if (outFile.exists() && (task.getDownSize() == outFile.length())) {
                append = true;
                downSize = task.getDownSize();
                connection.setRequestProperty("range", "bytes=" + downSize + "-");
            } else {
                FileUtil.delete(outFile);//if size not fit. delete it.
            }

            Messager.out("download downSize:" + downSize + ",fileSize:" + outFile.length() + ",append:" + append);

            connection.connect();
            /**
             * request OK
             */
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK
                    || connection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                InputStream is = connection.getInputStream();
                String valueAR = connection.getHeaderField("Accept-Ranges");
                String valueCR = connection.getHeaderField("Content-Range");
                /**
                 * if server not support range
                 * skip the data until target position
                 */
                if ((valueAR == null || valueAR.trim().equalsIgnoreCase("") || valueAR.equalsIgnoreCase("none"))
                        && (valueCR == null || valueCR.trim().equalsIgnoreCase("") || valueCR.equalsIgnoreCase("none"))) { //not support range
                    //cbOnError
                    cbOnError(ERROR, "not support range");
                    return;
                }

                byte[] buffer = new byte[BUFFER_SIZE];
                FileOutputStream os = new FileOutputStream(outFile, append);
                int size = 0;
                while ((task.getTargetStatus() == Task.Status.DOWNLOADING)
                        && (size = is.read(buffer)) > 0) {
                    os.write(buffer, 0, size);
                    downSize = downSize + size;
                    os.flush();
                    cbOnProgress(task.getTotalSize(), downSize);
                }
                os.flush();
                IOUtil.safeClose(is);
                IOUtil.safeClose(os);
                task.setDownSize(downSize);

                if (task.getTargetStatus() == Task.Status.PAUSE) {
                    cbOnPause();
                    return;
                }

                Messager.out("download all complete\n cost:" + (System.currentTimeMillis() - start));

                cbOnComplete(outFile.length());
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Messager.out("disconnect connection");
            if (connectionInfo != null && connectionInfo.getConnection() != null) {
                connectionInfo.getConnection().disconnect();
            }
        }
        cbOnError(ERROR, "unknown");
    }

    @Override
    protected byte getFlag() {
        return IDownloader.TYPE_NORMAL_DOWNLOADER;
    }

    /**
     * extends the save instance data
     *
     * @param os
     */
    @Override
    protected void writeExInstance(OutputStream os) {

    }

    @Override
    public Type getType() {
        return Type.NORMAL;
    }
}
