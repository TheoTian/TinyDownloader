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

import com.theo.downloader.AbstractDownloader;
import com.theo.downloader.DownloaderUtil;
import com.theo.downloader.IDownloader;
import com.theo.downloader.Task;
import com.theo.downloader.util.FileUtil;
import com.theo.downloader.util.IOUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.List;

/**
 * HLSDownloader
 *
 * @author: theotian
 * @since: 17/10/24
 * @describe:
 */
public class HLSDownloader extends AbstractDownloader {

    private PlaylistDownloader downloader;
    private MediaPlaylistTask mediaPlaylistTask;

    public HLSDownloader() {
        super();
    }

    public HLSDownloader(Task task) {
        super(task);
    }

    @Override
    protected void download(Task task) {

        /**
         * if downloader exist. continue download
         */
        if (downloader != null) {
            downloader.download();
            return;
        }
        /**
         * start download M3u8
         */
        if (!downloadM3u8(task)) {
            return;
        }
        /**
         * start download segments
         */
        downloadSegments(task);
    }

    /**
     * start download m3u8 file
     *
     * @param task
     * @return success or not
     */
    private boolean downloadM3u8(Task task) {
        HttpURLConnection connection = null;
        InputStream is = null;
        FileOutputStream os = null;

        try {
            String url = task.getRealUrl();
            connection = DownloaderUtil.createConnection(url);
            if (connection == null) {
                cbOnError(ERROR, "create connection failed");
                return false;
            }

            connection.connect();
            is = connection.getInputStream();
            if (is == null) {
                cbOnError(ERROR, "connection inputstream is null");
                return false;
            }

            String fileName = task.getFileName();
            String fileNameNoSuffix = fileName != null ? fileName.split("\\.")[0] : "";
            String filePath = task.getDstDir() + "/" + fileNameNoSuffix + "/" + fileName;
            task.setFilePath(filePath);
            FileUtil.checkAndCreateDir(new File(filePath).getParentFile());

            os = new FileOutputStream(filePath);
            byte[] buffer = new byte[BUFFER_SIZE];

            int size = 0;
            while ((size = is.read(buffer)) > 0) {
                os.write(buffer, 0, size);
                os.flush();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtil.safeClose(os);
            IOUtil.safeClose(is);
            if (connection != null) {
                connection.disconnect();
            }
        }
        return false;
    }

    /**
     * download segments
     *
     * @param task
     */
    private void downloadSegments(Task task) {
        if (task == null) {
            cbOnError(ERROR, "task null");
        }

        PlaylistParser parser = new PlaylistParser();
        File file = new File(task.getFilePath());
        FileInputStream is = null;

        try {
            is = new FileInputStream(file);
            Playlist playlist = parser.parse(task.getRealUrl(), is);

            if (playlist == null) {
                cbOnError(ERROR, "playlist is null");
                return;
            }

            if (playlist.getType() == Playlist.Type.MEDIA) {
                mediaPlaylistTask = new MediaPlaylistTask((MediaPlaylist) playlist, file.getParentFile().getAbsolutePath());
                downloader = new PlaylistDownloader(this, mediaPlaylistTask);
                downloader.download();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtil.safeClose(is);
        }
    }

    @Override
    public int pause() {
        super.pause();
        downloader.pause();
        return OK;
    }

    @Override
    protected byte getFlag() {
        return IDownloader.TYPE_HLS_DOWNLOADER;
    }

    @Override
    protected void writeExInstance(OutputStream os) throws IOException {

    }

    /**
     * update M3U8 URI
     */
    public void updateM3U8URI(MediaPlaylistTask tasks) {
        long start = System.currentTimeMillis();
        List<Task> downloadList = tasks.getCompelteList();
        String originName = task.getFilePath();

        File srcFile = new File(originName);
        File dstFile = new File(originName + ".tmp");

        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(srcFile)));
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dstFile)));
            String line;
            int index = 0;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(PlaylistParser.TAG_MEDIA_DURATION)) {
                    writer.write(line + "\n");
                    writer.flush();

                    line = reader.readLine();
                    if (line == null) {
                        break; //file end
                    }

                    if (!line.startsWith(PlaylistParser.TAG_PREFIX)) {
                        if (index < downloadList.size()) {
                            line = downloadList.get(index).getFileName();
                            index++;
                        }
                    }
                }

                writer.write(line + "\n");
                writer.flush();
            }
            IOUtil.safeClose(reader);
            IOUtil.safeClose(writer);

            FileUtil.rename(srcFile, srcFile.getAbsolutePath() + ".old");
            FileUtil.rename(dstFile, originName);

            System.out.println("updateM3U8URI cost:" + (System.currentTimeMillis() - start));
        } catch (Exception e) {
            e.printStackTrace();
            cbOnError(ERROR, "updateM3U8URI failed");
        } finally {
            IOUtil.safeClose(reader);
            IOUtil.safeClose(writer);
        }

    }
}
