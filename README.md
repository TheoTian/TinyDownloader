## TinyDownloader
[![license](http://img.shields.io/badge/license-BSD3-brightgreen.svg?style=flat)](./LICENSE)
[![release](http://img.shields.io/badge/release-v1.0-brightgreen.svg?style=flat)]()

This is a tiny downloader on Android, it just support http now. Will support more in the future.

There is two ways to download.

1.	Normal.

2. Multi-Segments.


## Getting started
See the demo code.

###CreateDownloader

```
mDownloader = DownloaderFactory.create(DownloaderFactory.Type.MULTI_SEGMENT, task);
if (mDownloader != null) {
    mDownloader.setListener(mDownloadListener);
    mDownloader.create();
}
```

###StartDownloader
```
mDownloader.start();
```

###PauseDownloader
```
mDownloader.pause();
```

###LoadDownloader

load the data(from onSaveInstance()) to create downloader.

resume download from the saved data.

```
 mDownloader = DownloaderFactory.load(FileUtil.readFile(new File(mTempPath)));
 if (mDownloader != null) {
     mDownloader.setListener(mDownloadListener);
 }
```



## Support
Any problem?

Contact me for help.

## License
Tinker is under the BSD license. See the [LICENSE](./LICENSE) file for details.

## Preview
<img src="asserts/show_demo.gif" width="180" height="320" alt="show_demo"/>

