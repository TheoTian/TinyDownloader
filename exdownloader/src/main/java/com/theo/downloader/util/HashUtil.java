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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Author: theotian
 * Date: 17/6/29
 * Describe:
 * Hash算法是指任意长度的字符串输入，此算法能给出固定n比特的字符串输出，输出的字符串一般称为Hash值。
 * <p>
 * 具有以下两个特点：
 * <p>
 * 抗碰撞性：寻找两个不同输入得到相同的输出值在计算上是不可行的，需要大约 的时间去寻找到具有相同输出的两个输入字符串。
 * 不可逆：不可从结果推导出它的初始状态。
 * 抗碰撞性使Hash算法对原始输入的任意一点更改，都会导致产生不同的Hash值，因此Hash算法可以用来检验数据的完整性。我
 * 们经常见到在一些网站下载某个文件时，网站还提供了此文件的hash值，以供我们下载文件后检验文件是否被篡改。
 * <p>
 * 不可逆的特性使Hash算法成为一种单向密码体制，只能加密不能解密，可以用来加密用户的登录密码等凭证。
 * <p>
 * 1.建议使用SHA-256、SHA-3算法。 如使用SHA-256算法对message字符串做哈希
 * <p>
 * 2.不建议使用MD2、MD4、MD5、SHA-1、RIPEMD算法来加密用户密码等敏感信息。这一类算法已经有很多破解办法，
 * 例如md5算法，网上有很多查询的字典库，给出md5值，可以查到加密前的数据。
 * <p>
 * 3.不要使用哈希函数做为对称加密算法的签名。
 * 4.注意：当多个字符串串接后再做hash，要非常当心
 * 如：字符串S，字符串T，串接做hash，记为 H (S||T)。但是有可能发生以下情况。
 * 如“builtin||securely” 和 “built||insecurely”的hash值是完全一样的。
 * <p>
 * 如何修改从而避免上述问题产生？ 改为H(length(S) || S || T)或者 H(H(S)||H(T))或者H(H(S)||T)。
 * <p>
 * 实际开发过程中经常会对url的各个参数，做词典排序，然后取参数名和值串接后加上某个SECRET字符串，计算出hash值，
 * 作为此URL的签名，如foo=1, bar=2, baz=3 排序后为bar=2, baz=3, foo=1，做hash的字符串为：
 * SECRETbar2baz3foo1，在参数和值之间没有分隔符，则”foo=bar”和”foob=ar”的hash值是一样的，
 * ”foo=bar&fooble=baz”和”foo=barfooblebaz”一样，这样通过精心构造的恶意参数就有可能与正常参数的hash值一样，
 * 从而骗过服务器的签名校验。
 */

public class HashUtil {

    /**
     * SHA256加密String
     *
     * @param string
     * @return Hex
     */
    public static String SHA256Encrypt(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("SHA-256").digest(
                    string.getBytes("UTF-8"));
            return StringUtil.getHexString(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
        }
        return "";
    }


    /**
     * MD5加密String
     *
     * @param string
     * @return Hex
     */
    public static String MD5Encrypt(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(
                    string.getBytes("UTF-8"));
            return StringUtil.getHexString(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
        }
        return "";
    }

    /**
     * MD5加密文件
     *
     * @param inputFile
     * @return
     * @throws IOException
     */
    public static String FileMD5Encrypt(String inputFile) throws IOException {
        // 缓冲区大小（这个可以抽出一个参数）
        int bufferSize = 256 * 1024;
        FileInputStream fileInputStream = null;
        DigestInputStream digestInputStream = null;

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            fileInputStream = new FileInputStream(inputFile);
            digestInputStream = new DigestInputStream(fileInputStream, messageDigest);

            // read的过程中进行MD5处理，直到读完文件
            byte[] buffer = new byte[bufferSize];
            while (digestInputStream.read(buffer) > 0) ;

            messageDigest = digestInputStream.getMessageDigest();
            // 拿到结果，也是字节数组，包含16个元素

            byte[] resultByteArray = messageDigest.digest();
            // 同样，把字节数组转换成16进制字符串
            return StringUtil.getHexString(resultByteArray);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally {
            try {
                digestInputStream.close();
            } catch (Exception e) {
            }
            try {
                fileInputStream.close();
            } catch (Exception e) {
            }
        }
        return null;
    }
}
