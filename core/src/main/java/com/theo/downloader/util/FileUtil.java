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

public class FileUtil {

    /**
     * check & create the directory
     *
     * @param dir
     * @return
     */
    public static boolean checkAndCreateDir(File dir) {
        if (dir == null) {
            return false;
        }
        if (dir.exists() && dir.isFile()) {
            return false;
        }
        if (!dir.exists()) {
            return dir.mkdirs();
        }
        return true;
    }

    /**
     * 监测并且创建新文件
     *
     * @return
     */
    public static boolean checkAndCreateFile(File file) {
        if (file == null) {
            return false;
        }

        if (!file.exists()) {
            try {
                return file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    /**
     * delete file
     *
     * @param file
     */
    public static void delete(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }


    /**
     * 写对象到文件
     *
     * @param file
     * @param obj
     */
    public static void writeObjectToFile(File file, Object obj) {
        if (file == null) {
            return;
        }
        checkAndCreateDir(file.getParentFile());
        checkAndCreateFile(file);
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            ObjectOutputStream objOut = new ObjectOutputStream(out);
            objOut.writeObject(obj);
            objOut.flush();
            objOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * read file
     * file length must < Integer.MAX_VALUE
     *
     * @param file
     * @return
     */
    public static byte[] readFile(File file) {
        if (file == null && file.length() > Integer.MAX_VALUE) {
            return null;
        }
        byte[] data = new byte[(int) file.length()];

        try {
            FileInputStream is = new FileInputStream(file);
            is.read(data);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * rename file to new name
     *
     * @param file
     * @param pathname
     * @return
     */
    public static boolean rename(File file, String pathname) {
        if (file == null) {
            return false;
        }
        return file.renameTo(new File(pathname));
    }

}
