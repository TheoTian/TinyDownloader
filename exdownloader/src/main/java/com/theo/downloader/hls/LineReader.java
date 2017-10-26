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

import java.io.BufferedReader;
import java.util.Queue;

/**
 * LineReader
 *
 * @author: theotian
 * @since: 17/10/24
 * @describe:
 */
public class LineReader {

    private BufferedReader reader;
    private Queue<String> extraLine;
    private String next;

    public LineReader(Queue<String> extraLine, BufferedReader reader) {
        this.extraLine = extraLine;
        this.reader = reader;
    }

    /**
     * whether have next line
     *
     * @return
     */
    public boolean hasNext() {
        if (next != null) {
            return true;
        }
        /**
         * extraLine has next line
         */
        if (extraLine != null && extraLine.size() > 0) {
            next = extraLine.poll();
            return true;
        }
        /**
         * read next from reader
         */
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    next = line;
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    /**
     * getNext Line
     *
     * @return
     */
    public String getNext() {
        if (hasNext()) {
            String result = next;
            next = null;
            return result;
        }
        return null;
    }
}
