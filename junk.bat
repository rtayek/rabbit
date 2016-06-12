set package=com.tayek.tablet.gui.android.cb7
::setlocal enabledelayedexpansion
for %%i in (0a9196e8) do (
	adb devices -l | grep %%i >nul
	if errorlevel 1 (
    	echo %%i is not connected!
	) else (
		echo %%i is connected.
		adb -s %%i shell run-as %package% ls -l /data/data/%package%/files
		adb -s %%i exec-out run-as %package% cat /data/data/%package%/files/tablet.0.0.log 2>%%i.0.0.errors.txt 1> %%i.0.0.log
		adb -s %%i exec-out run-as %package% cat /data/data/%package%/files/tablet.1.0.log 2>%%i.1.0.errors.txt 1> %%i.1.0.log
	)
)
