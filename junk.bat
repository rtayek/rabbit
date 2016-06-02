setlocal enabledelayedexpansion
for %%i in (0a9196e8) do (
	set device=%%i
	set package=com.tayek.tablet.gui.android.cb7
	adb devices -l | grep %device% >nul
	if errorlevel 1 (
    	echo %device% is not connected!
	) else (
		echo %device% is connected.
		adb -s %device% shell run-as %package% ls -l /data/data/%package%/files
		adb -s %device% exec-out run-as %package% cat /data/data/%package%/files/tablet.0.0.log 2>%device%.0.0.errors.txt 1> %device%.0.0.log
		adb -s %device% exec-out run-as %package% cat /data/data/%package%/files/tablet.1.0.log 2>%device%.1.0.errors.txt 1> %device%.1.0.log
	)
)
