set device=%1
set package=com.tayek.tablet.gui.android.cb7
adb devices -l | grep %device% >nul
if errorlevel 1 (
    echo %device% is not connected!
) else (
	echo %device% is connected.
	echo deleting:
	adb -s %device% shell run-as %package% ls -l /data/data/%package%/files
	adb -s %device% run-as %package% rm /data/data/%package%/files/tablet.*.log
	)
	
)
