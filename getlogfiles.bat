set device=%1
set package=com.tayek.tablet.gui.android.cb7
adb devices -l | grep %device% >nul
if errorlevel 1 (
    echo %device% is not connected!
) else (
	echo %device% is connected.
	adb -s %device% shell run-as %package% pwd
	adb -s %device% shell run-as %package% df
	adb -s %device% shell run-as %package% du -h
	adb -s %device% shell run-as %package% du -h /data/data/%package%
	adb -s %device% shell run-as %package% ls -l /data/data/%package%
	adb -s %device% shell run-as %package% ls -l /data/data/%package%/files
	for /l %%i in (0,1,9) do adb -s %device% exec-out run-as %package% cat /data/data/%package%/files/tablet0.%%i.log > %device%0.%%i.log
)
