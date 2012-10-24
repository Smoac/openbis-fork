Prerequisites
=============

Before you can run the tests, you need to have
* Firefox in your PATH environment variable
* Prepare a copy of the application war.


Firefox in PATH in Eclipse
============================

To have Firefox available in your PATH in Eclipse (when started from a launcher), you need to
set the PATH variable value in file ~/.MacOSX/environment.plist

Example:
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
	<key>PATH</key>
	<string>/Users/anttil/bin:/opt/local/bin:/opt/local/sbin:/scripts:/usr/texbin:/opt/local/bin:/opt/local/sbin:/usr/bin:/bin:/usr/sbin:/sbin:/usr/local/bin:/opt/X11/bin:/usr/texbin:/usr/X11/bin:$PATH:/Applications/Firefox 10.app/Contents/MacOS:/System/Library/Frameworks/JavaVM.framework/Versions/Current:/System/Library/Frameworks/JavaVM.framework/Versions/Current:/Applications/Firefox 10.app/Contents/MacOS</string>
</dict>
</plist>

Note: using "$PATH" in this file does not work. This is why I pasted my whole PATH content in the file.


Creating copy of application war
================================

Run target "create-webapp" from ui-test build file. This needs to done also when client code is updated.

(On CI server, this target is executed before every test run)


How to run the tests against already running openBIS?
=====================================================

See method SeleniumTest.initialization(). Variables asUrl and dssUrl control which AS/DSS instance 
the tests are run against. If those variables are not set, new AS and DSS will be started.

In comments in that method, there are few examples.