About
-----

Paranoid over the air updates app


License
-------

ParanoidOTA is licensed under the terms of the *GNU General Public License,
version 3.0*. See the *COPYING* file for the full license text.


Using the app
-------------

ParanoidOTA allows you to update your Paranoid Android ROM and your Google Apps.
If an update is found, you'll receive a notification. Click it to open the app.
Go to the Updates tab and click on the file you want to download.
Once the download finishes, it will be added to the Install tab. Click the 
Install button and select the options you want to perform. Your device will reboot 
into recovery to install the updates.
Only TWRP and CWM-based recoveries are supported. Closed source CWM is not supported.


Building the app
----------------

ParanoidOTA needs to be installed in /system/priv-app to achieve the system
permissions it needs. If you want to debug the app with Eclipse, first
delete the app from /data/app if you had it there and move it to /system/priv-app,
grant the file with the necessary permissions and reboot. Then increment the version
code in AndroidManifest.xml and the app will run with the right permissions. 


-EOF-