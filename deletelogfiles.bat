set device=%1
set package=com.tayek.tablet.gui.android.cb7
adb devices -l | grep %device% >nul
if errorlevel 1 (
    echo %device% is not connected!
) else (
	echo %device% is connected.
	adb -s %device% shell run-as %package% ls -l /data/data/%package%/files
	for /l %%i in (0,1,9) do adb -s %device% exec-out run-as %package% rm /data/data/%package%/files/tablet.0.%%i.log
	for /l %%i in (0,1,9) do adb -s %device% exec-out run-as %package% rm /data/data/%package%/files/tablet.1.%%i.log
)
