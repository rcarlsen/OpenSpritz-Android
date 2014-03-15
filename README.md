# OpenSpritz-Android

![OpenSpritz](http://i.imgur.com/3ACFJ5s.gif)

OpenSpritz-Android is a Spritz-like .epub reader for Android 3.0+ (API 11). Inspired by Miserlou's [OpenSpritz](https://github.com/Miserlou/OpenSpritz).

This branch contains modifications to make the app function (somewhat)
using Glass; the modifications are mostly to support keyboard navigation
(Tab / Shift-tab for the trackpad swipes) and do not include the Glass
Development Kit enhancements.

In the absence of a file chooser on Glass, an epub path to a resource in
the raw directory has been hard-coded (this resource has not been
committed to the repo; you will need to supply your own epub file as a
raw resource and update the URI accordingly - SpritzFragment.java:243).

## Lend a hand

#### Enhancements

+ Fortify epub parsing to whatever extent is possible with epublib
    + CSS markup isn't currently stripped
+ More sophisticated pivot choosing

#### Features

+ It'd be nice to digest `http://...` share intents, parse & spritz the resulting page.
+ Read text from clipboard

## Building

0. Make sure you've installed the following from the Android SDK Manager before building:
  	+ Android SDK Build-tools 19.0.2
	+ Android SDK tools 22.3
	+ SDK Platform 19
	+ Android Support Repository 4
	
1. Define the `ANDROID_HOME` environmental variable as your Android SDK location.
	
	If you need help check out [this guide](http://spring.io/guides/gs/android/).

3. Build!
	
  	To build an .apk from this directory, make sure `./gradlew` is executable and run:

    	$ ./gradlew assemble
    
	The resulting .apk will be availble in `./app/build/apk`.


## Thanks

+ [epublib](https://github.com/psiegman/epublib) by psiegman (LGPL)

#### A Note About the Name

OpenSpritz has nothing to do with [Spritz Incorporated](http://www.spritzinc.com/). This is an open source, community created project, made with love because Spritz is such an awesome technique for reading with.

## License

GPLv3
