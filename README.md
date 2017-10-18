## TinyDownloader
[![license](http://img.shields.io/badge/license-BSD3-brightgreen.svg?style=flat)](./LICENSE)
[![release](http://img.shields.io/badge/release-v1.0-brightgreen.svg?style=flat)]()

See [中文介绍](https://github.com/TheoTian/TinyDownloader/wiki/TinyDownloader%E4%BB%8B%E7%BB%8D)

This is a tiny downloader on Android, it just support http now. Will support more in the future. 

There is two ways to download.

1.	Normal.
	> Download bytes with one thread.

2.  Multi-Segments.
	> Divide the resource into multi segments.
	> every segment has one thread to download.


## Getting started
See the demo code to get details.

#### CreateDownloader

```
mDownloader = DownloaderFactory.create(DownloaderFactory.Type.MULTI_SEGMENT, task);
if (mDownloader != null) {
    mDownloader.setListener(mDownloadListener);
    mDownloader.create();
}
```

#### StartDownloader

```
mDownloader.start();
```

#### PauseDownloader
```
mDownloader.pause();
```

#### LoadDownloader

Load the resume-data to create downloader.
resume-data comes from onSaveInstance callback.

```
 mDownloader = DownloaderFactory.load(FileUtil.readFile(new File(mTempPath)));
 if (mDownloader != null) {
     mDownloader.setListener(mDownloadListener);
 }
```
#### DownloaderListener
```
interface DownloadListener {

        void onCreated(Task task, SnifferInfo snifferInfo);

        void onStart(Task task);

        void onPause(Task task);

        void onProgress(Task task, long total, long down);

        void onError(Task task, int error, String msg);

        void onComplete(Task task, long total);

        /**
         * callback resume-data
         * this will call after paused.
         * you can use the data to load and continue download from paused position.
         *
         * @param task
         * @param data resume data
         */
        void onSaveInstance(Task task, byte[] data);
    }
```

## Support
Any problem?

Contact me for help.

## License
Tinker is under the BSD license. See the [LICENSE](./LICENSE) file for details.

## Preview
<img src="asserts/show_demo.gif" width="180" height="320" alt="show_demo"/>

