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

import com.theo.downloader.util.CharUtil;
import com.theo.downloader.util.IOUtil;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;

/**
 * PlaylistParser
 *
 * @author: theotian
 * @since: 17/10/24
 *
 */
public class PlaylistParser {

    public static final String TAG_PREFIX = "#EXT";

    /**
     * Basic Tags
     */
    private static final String PLAYLIST_HEADER = "#EXTM3U";
    private static final String TAG_VERSION = "#EXT-X-VERSION";

    /**
     * Media Segment Tags
     */
    public static final String TAG_MEDIA_DURATION = "#EXTINF";
    private static final String TAG_BYTERANGE = "#EXT-X-BYTERANGE";
    private static final String TAG_DISCONTINUITY = "#EXT-X-DISCONTINUITY";
    private static final String TAG_KEY = "#EXT-X-KEY";
    private static final String TAG_INIT_SEGMENT = "#EXT-X-MAP";
    private static final String TAG_PROGRAM_DATE_TIME = "#EXT-X-PROGRAM-DATE-TIME";

    /**
     * Media Playlist Tags
     */
    private static final String TAG_TARGET_DURATION = "#EXT-X-TARGETDURATION";
    private static final String TAG_MEDIA_SEQUENCE = "#EXT-X-MEDIA-SEQUENCE";
    private static final String TAG_DISCONTINUITY_SEQUENCE = "#EXT-X-DISCONTINUITY-SEQUENCE";
    private static final String TAG_ENDLIST = "#EXT-X-ENDLIST";
    private static final String TAG_PLAYLIST_TYPE = "#EXT-X-PLAYLIST-TYPE";

    /**
     * Master Playlist Tags
     */
    private static final String TAG_MEDIA = "#EXT-X-MEDIA";
    private static final String TAG_STREAM_INF = "#EXT-X-STREAM-INF";

    /**
     * Media or Master Playlist Tags
     */
    private static final String TAG_INDEPENDENT_SEGMENTS = "#EXT-X-INDEPENDENT-SEGMENTS";
    private static final String TAG_START = "#EXT-X-START";

    /**
     * parse the data from input stream
     *
     * @param is inputstream
     * @return playlist media playlist or master playlist
     * @throws IOException IOException
     */
    public Playlist parse(final String uri, final InputStream is) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        Queue<String> extraLine = new LinkedList<>();

        try {
            if (!containHLSHeader(reader)) {
                return null;
            }
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty()) {
                    //do nothing
                } else if (line.startsWith(TAG_STREAM_INF)) {
                    //master playlist
                    return parseMasterPlaylist();// TODO: 17/10/24 support in the future
                } else if (line.startsWith(TAG_TARGET_DURATION)
                        || line.startsWith(TAG_MEDIA_SEQUENCE)
                        || line.startsWith(TAG_MEDIA_DURATION)
                        || line.startsWith(TAG_KEY)
                        || line.startsWith(TAG_BYTERANGE)
                        || line.equals(TAG_DISCONTINUITY)
                        || line.equals(TAG_DISCONTINUITY_SEQUENCE)
                        || line.equals(TAG_ENDLIST)) {
                    //media playlist
                    return parseMediaPlaylist(uri, new LineReader(extraLine, reader));
                } else {
                    extraLine.add(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtil.safeClose(reader);
        }
        return null;
    }

    /**
     * parse master playlist.
     * todo support in the future
     *
     * @return
     */
    private Playlist parseMasterPlaylist() {
        return null;
    }

    /**
     * parse media playlist
     *
     * @return
     */
    private Playlist parseMediaPlaylist(String uri, LineReader lineReader) {
        MediaPlaylist playlist = new MediaPlaylist(uri);

        int index = 0;
        String hostpath = uri.substring(0, (index = uri.lastIndexOf("/")) != -1 ? index : 0);

        while (lineReader != null && lineReader.hasNext()) {
            String line = lineReader.getNext();
            if (line.startsWith(TAG_MEDIA_DURATION) && !(line = lineReader.getNext()).startsWith(TAG_PREFIX)) {
                String segUrl = line;
                try {
                    new URL(line);
                } catch (MalformedURLException e) {
                    //not a url
                    segUrl = hostpath + "/" + line;
                }

                System.out.println(segUrl);
                playlist.add(new MediaPlaylist.Segment(segUrl));
            }
        }
        return playlist;
    }

    /**
     * check whether the input data is hls
     *
     * @param reader
     * @return
     */
    private boolean containHLSHeader(Reader reader) throws IOException {
        if (reader == null) {
            return false;
        }

        int latest = skipUselessChar(reader.read(), reader);
        if (CharUtil.isEnd(latest)) {
            return false;
        }

        char[] headerChars = PLAYLIST_HEADER.toCharArray();

        /**
         * check whether map the header chars
         */
        for (char headerChar : headerChars) {
            if (latest != headerChar) {
                return false;
            }
            latest = reader.read();
        }

        skipWhiteSpace(latest, reader, false);

        return true;
    }

    /**
     * invisible char 0xEF 0xBB 0xBF
     *
     * @param reader
     * @return -1 error or end of the file. != -1
     */
    private int skipBOMHeader(int latest, Reader reader) throws IOException {
        if (latest == 0xEF) {
            if (reader.read() != 0xBB || reader.read() != 0xBF) {
                return -1;
            }
            return reader.read();
        }
        return latest;
    }

    /**
     * skip white space
     *
     * @param latest
     * @param reader
     * @return
     * @throws IOException
     */
    private int skipWhiteSpace(int latest, Reader reader) throws IOException {
        return skipWhiteSpace(latest, reader, true);
    }

    private int skipWhiteSpace(int latest, Reader reader, boolean skipLinebreaks) throws IOException {
        //skip the space char
        while (!CharUtil.isEnd(latest) && Character.isWhitespace(latest)
                && (skipLinebreaks || !CharUtil.isLinebreak(latest))) {
            latest = reader.read();
        }
        return latest;
    }

    /**
     * skip useless char like BOM char and linebreak and white space
     *
     * @param reader
     * @return latest char
     */
    private int skipUselessChar(int latest, Reader reader) throws IOException {
        latest = skipBOMHeader(latest, reader);
        if (CharUtil.isEnd(latest)) {
            return -1;
        }
        latest = skipWhiteSpace(latest, reader);
        return latest;
    }


}
