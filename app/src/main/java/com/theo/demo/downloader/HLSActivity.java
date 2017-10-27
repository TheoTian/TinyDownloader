package com.theo.demo.downloader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.theo.downloader.DownloaderFactory;
import com.theo.downloader.IDownloader;
import com.theo.downloader.Task;
import com.theo.downloader.hls.HLSDownloader;
import com.theo.downloader.info.SnifferInfo;
import com.theo.downloader.util.FileUtil;

import java.io.File;
import java.io.FileOutputStream;

public class HLSActivity extends AppCompatActivity {

    private final String mUrl = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_4x3/gear1/prog_index.m3u8";

    private String mTempPath;

    private IDownloader mDownloader;
    private ProgressBar mProgressBar;
    private TextView mTvDownSpeed;
    private EditText mEtUrl;
    private TextView mTvResult;
    /**
     * downloader listener
     */
    private IDownloader.DownloadListener mDownloadListener = new IDownloader.DownloadListener() {
        /**
         * when download task created.after sniffer the net
         * @param task
         * @param snifferInfo
         */
        @Override
        public void onCreated(Task task, SnifferInfo snifferInfo) {
            System.out.println("onCreated realUrl:" + snifferInfo.realUrl);
            System.out.println("onCreated contentLength:" + snifferInfo.contentLength);
        }

        /**
         * task start to download
         * @param task
         */
        @Override
        public void onStart(Task task) {
            System.out.println("onStart");
        }

        /**
         * task pause to download
         * @param task
         */
        @Override
        public void onPause(Task task) {
            System.out.println("onPause");
        }

        /**
         * download progress
         * @param task task
         * @param total total bytes ps: HLS type not support this progress.use {@link com.theo.downloader.hls.HLSDownloader#setMediaSegmentListener(IDownloader.DownloadListener)}
         * @param down bytes down
         */
        @Override
        public void onProgress(final Task task, final long total, final long down) {

        }

        /**
         * throw error
         * @param task
         * @param error
         * @param msg
         */
        @Override
        public void onError(Task task, int error, String msg) {
            System.out.println("onError [" + error + "," + msg + "]");
        }

        /**
         * task complete
         * @param task
         * @param total
         */
        @Override
        public void onComplete(final Task task, final long total) {
            System.out.println("onComplete [" + total + "]");
            RunnableUtil.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    String result = "";
                    result += "Download Complete!\n";
                    result += "Path: " + task.getFilePath() + "\n";
                    result += "Length: " + total + "\n";
                    mTvResult.setText(result);
                }
            });
        }

        /**
         * save the instance.when you wanna continue download next time.
         * @param task
         * @param data 断点续传数据
         */
        @Override
        public void onSaveInstance(Task task, byte[] data) {
            saveInstanceToFile(new File(mTempPath), data);
        }

    };


    /**
     * segment downloader listener
     */
    private IDownloader.DownloadListener mSegmentdDownloadListener = new IDownloader.DownloadListener() {
        /**
         * when download task created.after sniffer the net
         * @param task
         * @param snifferInfo
         */
        @Override
        public void onCreated(Task task, SnifferInfo snifferInfo) {
            System.out.println("Task[" + task.getIndex() + "] onCreated realUrl:" + snifferInfo.realUrl);
            System.out.println("Task[" + task.getIndex() + "] onCreated contentLength:" + snifferInfo.contentLength);
        }

        /**
         * task start to download
         * @param task
         */
        @Override
        public void onStart(Task task) {
            System.out.println("Task[" + task.getIndex() + "] onStart");
        }

        /**
         * task pause to download
         * @param task
         */
        @Override
        public void onPause(Task task) {
            System.out.println("Task[" + task.getIndex() + "] onPause");
        }

        /**
         * download progress
         * @param task task
         * @param total total bytes ps: HLS type not support this progress.use {@link com.theo.downloader.hls.HLSDownloader#setMediaSegmentListener(IDownloader.DownloadListener)}
         * @param down bytes down
         */
        @Override
        public void onProgress(final Task task, final long total, final long down) {
            RunnableUtil.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    updateProgress(total, down);
                    updateDownSpeed(task);
                }
            });
        }

        /**
         * throw error
         * @param task
         * @param error
         * @param msg
         */
        @Override
        public void onError(Task task, int error, String msg) {
            System.out.println("Task[" + task.getIndex() + "] onError [" + error + "," + msg + "]");
        }

        /**
         * task complete
         * @param task
         * @param total
         */
        @Override
        public void onComplete(final Task task, long total) {
            System.out.println("Task[" + task.getIndex() + "] onComplete [" + total + "]");
        }

        /**
         * if is setSegmentListener this will never callback
         * @param task
         * @param data 断点续传数据
         */
        @Override
        public void onSaveInstance(Task task, byte[] data) {
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hls);
        mTempPath = getExternalCacheDir().getAbsolutePath() + "/bundle.tmp";//you should save the path to Persistence way like DB or file
        initView();
    }

    private void initView() {
        findViewById(R.id.btCreate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCreateClick();
            }
        });
        findViewById(R.id.btStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartClick();
            }
        });
        findViewById(R.id.btPause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPauseClick();
            }
        });
        findViewById(R.id.btLoad).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoadClick();
            }
        });

        mProgressBar = (ProgressBar) findViewById(R.id.pbProgress);
        mTvDownSpeed = (TextView) findViewById(R.id.tvDownSpeed);
        mEtUrl = (EditText) findViewById(R.id.etUrl);
        mEtUrl.setText(mUrl);

        mTvResult = (TextView) findViewById(R.id.tvResult);
    }

    private void onCreateClick() {
        Task task = new Task(mEtUrl.getText().toString(), mEtUrl.getText().toString(), getExternalCacheDir().getAbsolutePath());
        mDownloader = DownloaderFactory.create(IDownloader.Type.HLS, task);
        if (mDownloader != null) {
            mDownloader.setListener(mDownloadListener);
            if (mDownloader instanceof HLSDownloader) {
                ((HLSDownloader) mDownloader).setMediaSegmentListener(mSegmentdDownloadListener);
            }
            mDownloader.create();
        }
    }

    private void onStartClick() {
        if (mDownloader != null) {
            mDownloader.start();
        }
    }

    private void onPauseClick() {
        if (mDownloader != null) {
            mDownloader.pause();
        }
    }

    private void onLoadClick() {
        mDownloader = DownloaderFactory.load(FileUtil.readFile(new File(mTempPath)));
        if (mDownloader != null) {
            mDownloader.setListener(mDownloadListener);
            if (mDownloader instanceof HLSDownloader) {
                ((HLSDownloader) mDownloader).setMediaSegmentListener(mSegmentdDownloadListener);
            }
        }
    }

    private void saveInstanceToFile(File file, byte[] data) {
        System.out.println("onSaveInstance data.length:" + data.length);
        try {
            FileOutputStream os = new FileOutputStream(file);
            os.write(data);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateProgress(long total, long down) {
        if (mProgressBar != null) {
            int progress = (int) (down * mProgressBar.getMax() / total);
            mProgressBar.setProgress(progress);
        }
    }

    /**
     * convert yourself
     *
     * @param task
     */
    private void updateDownSpeed(Task task) {
        long speed = task.getDownSpeed();
        String speedText;
        if (speed < 1024) { //you can also convert to double
            speedText = speed + " B/s";
        } else if (speed < 1024 * 1024) {
            speedText = (speed / 1024) + " KB/S";
        } else {
            speedText = (speed / (1024 * 1024)) + " MB/S";
        }

        if (mDownloader instanceof HLSDownloader) {
            int taskCounter = ((HLSDownloader) mDownloader).getTasksCount();
            int index = task.getIndex() + 1;

            String name = task.getFileName();

            if (mTvDownSpeed != null) {
                mTvDownSpeed.setText("(" + index + "/" + taskCounter + ") " + name + " " + speedText);
            }
        } else {
            if (mTvDownSpeed != null) {
                mTvDownSpeed.setText(speedText);
            }
        }
    }
}
