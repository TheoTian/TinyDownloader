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

/**
 * Author: theotian
 * Date: 17/6/22
 * Describe:
 */

public class StringUtil {

    private static char[] HEX = {'0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * convert byte to hex string
     *
     * @return hex string
     */
    public static String getHexString(byte[] data) {
        if (data == null || data.length <= 0) {
            return null;
        }
        char[] hex = new char[data.length * 2];
        int index = 0;
        for (int i = 0; i < data.length; i++) {
            hex[index++] = HEX[data[i] >> 4 & 0xf];
            hex[index++] = HEX[data[i] & 0xf];
        }
        return new String(hex);
    }

    /**
     * convert from hex to byte
     *
     * @return byte array. error return null
     */
    public static byte[] HexToByte(String hexStr) {
        if (hexStr == null || hexStr.trim().equals("")) {
            return null;
        }
        char[] hexChars = hexStr.toLowerCase().toCharArray(); //统一换为小写
        if (hexChars.length % 2 != 0) { //必须是2的倍数
            return null;
        }
        byte[] data = new byte[hexChars.length / 2];
        for (int i = 0; i < hexChars.length; i = i + 2) {
            data[i / 2] = (byte) (((HexToDec(hexChars[i]) << 4) & 0xf0) | (HexToDec(hexChars[i + 1]) & 0x0f));
        }
        return data;
    }


    private final static int ASCII_0 = 48;
    private final static int ASCII_9 = 57;
    private final static int ASCII_A = 65;
    private final static int ASCII_F = 70;
    private final static int ASCII_a = 97;
    private final static int ASCII_f = 102;
    /**
     * convert char hex to int.
     * eg. 0xF -> (int)16
     *
     * @param hex must lower case like a->f
     * @return -1 invalid char.not allow to hex
     */
    private static int HexToDec(char hex) {
        if (hex >= ASCII_0 && hex <= ASCII_9) {
            return (hex - ASCII_0);
        }
        if (hex >= ASCII_A && hex <= ASCII_F) {
            return (hex - ASCII_A) + 10;
        }
        if (hex >= ASCII_a && hex <= ASCII_f) {
            return (hex - ASCII_a) + 10;
        }
        return -1;
    }

}
