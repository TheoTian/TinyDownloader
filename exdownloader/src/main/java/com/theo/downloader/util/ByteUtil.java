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

package com.theo.downloader.util;

import java.io.*;
import java.nio.ByteBuffer;

public class ByteUtil {

    /**
     * write Serializable object into byte array
     *
     * @param obj
     */
    public synchronized static byte[] writeObjectToBytes(Object obj) {
        ByteArrayOutputStream out;
        try {
            out = new ByteArrayOutputStream();
            ObjectOutputStream objOut = new ObjectOutputStream(out);
            objOut.writeObject(obj);
            objOut.flush();
            IOUtil.safeClose(objOut);

            byte[] bytes = out.toByteArray();
            out.flush();
            IOUtil.safeClose(out);
            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * read from
     * @param bytes
     * @return
     */
    public synchronized static Object readObjectFromBytes(byte[] bytes) {
        Object object = null;
        ByteArrayInputStream in;
        try {
            in = new ByteArrayInputStream(bytes);
            ObjectInputStream objIn = new ObjectInputStream(in);
            object = objIn.readObject();
            objIn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return object;
    }

    /**
     * cast int to byte array
     * big endian
     *
     * @param integer
     * @return
     */
    public static byte[] cast(int integer) {
        byte[] intBytes = new byte[4];
        for (int i = (intBytes.length - 1); i >= 0; i--) {
            intBytes[i] = (byte) (integer >> ((intBytes.length - 1 - i) * 8));
        }
        return intBytes;
    }

    /**
     * cast byte array into int
     * big endian
     *
     * @param intBytes
     * @return
     */
    public static int cast(byte[] intBytes) {
        byte[] realSource = new byte[4];
        if (intBytes == null || intBytes.length == 0) {
            return 0;
        }
        //fill the high bit in the low array index
        System.arraycopy(intBytes, intBytes.length >= realSource.length ? (intBytes.length - realSource.length) : 0,
                realSource, intBytes.length >= realSource.length ? 0 : (realSource.length - intBytes.length), Math.min(intBytes.length, realSource.length));

        int result = 0;
        for (int i = (realSource.length - 1); i >= 0; i--) {
            result += (realSource[i] & 0xFF) << ((realSource.length - 1 - i) * 8);//0XFF补码高位还原
        }
        return result;
    }
}
