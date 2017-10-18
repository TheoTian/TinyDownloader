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
import com.theo.downloader.info.SnifferInfo;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloaderUtil {

    /**
     * get the resource realUrl & contentLength
     * must run in the sub thread
     *
     * @param url
     */
    public static SnifferInfo sniffer(String url) {
        SnifferInfo snifferInfo = new SnifferInfo();
        snifferInfo.realUrl = url;
        HttpURLConnection connection = null;
        try {
            connection = createConnection(new URL(url));
            connection.connect();

            while (connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP
                    || connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM) {
                connection.disconnect();
                snifferInfo.realUrl = connection.getHeaderField("Location");
                connection = createConnection(new URL(snifferInfo.realUrl));
                if (connection == null) {
                    return snifferInfo;
                }
                connection.connect();
            }
            snifferInfo.contentLength = connection.getHeaderField("Content-Length");
            return snifferInfo;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return snifferInfo;
    }

    /**
     * create connection
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static HttpURLConnection createConnection(String url) throws IOException {
        if (url == null || url.trim().equalsIgnoreCase("")) {
            return null;
        }
        return createConnection(new URL(url));
    }

    private static HttpURLConnection createConnection(URL url) throws IOException {
        if (url == null) {
            return null;
        }
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (connection == null) {
            return null;
        }
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("User-Agent", "BakuClient(version:v1.0.0)");
        return connection;
    }

    /**
     * create connection info
     *
     * @param urlStr
     * @return
     * @throws IOException
     */
    public static ConnectionInfo createConnectionInfo(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        if (url == null || !url.getProtocol().equalsIgnoreCase("http")) {
            return null;
        }
        return new ConnectionInfo().setConnection(createConnection(url)).setUrl(url);
    }

    /**
     * skip bytes
     *
     * @param is
     * @param skipSize
     */
    public static void skip(InputStream is, long skipSize) throws IOException {
        /**
         * skip the byte already down
         */
        long skipped = 0;
        while (skipped < skipSize) {
            skipped += is.skip(skipSize - skipped);
        }
    }
}
