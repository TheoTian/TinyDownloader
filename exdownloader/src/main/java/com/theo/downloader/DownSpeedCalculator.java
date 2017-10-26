package com.theo.downloader;

/**
 * Author: theotian
 * Date: 17/10/17
 * Describe:
 */

public class DownSpeedCalculator {

    private long startRecord = -1;//开始记录当前下载位置
    private long startTimeStamp = -1;
    private Task mTask;

    public DownSpeedCalculator(Task task) {
        mTask = task;
    }

    /**
     * 开始计算下载速度
     */
    protected void startCalculateDownSpeed() {
        if (startRecord != -1 || startTimeStamp != -1 || mTask == null) {//前一次采集未结束
            return;
        }
        startRecord = mTask.getDownSize();
        startTimeStamp = System.currentTimeMillis();
        return;
    }

    /**
     * 停止计算下载速度并记录
     */
    protected void endCalculateDownSpeed() {
        long duration = (System.currentTimeMillis() - startTimeStamp) / 1000;

        if (startRecord == -1 || startTimeStamp == -1 || duration < 1 || mTask == null) { //采集事件至少大于1s
            return;
        }

        long downBytes = mTask.getDownSize() - startRecord;
        mTask.setDownSpeed(downBytes / duration);
        startRecord = -1;
        startTimeStamp = -1;
    }
}
