::setlocal enabledelayedexpansion
::for %%i in (0a9196e8 0ab62080 0ab63506 0ab62207 0b029b33 0ab61d9b 0b03ae31 015d2109aa080e1a) do (
for %%i in (0a9196e8) do (
	set device=%%i
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
	)
)