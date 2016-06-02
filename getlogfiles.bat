set device=%1
set package=com.tayek.tablet.gui.android.cb7
adb devices -l | grep %device% >nul
if errorlevel 1 (
    echo %device% is not connected!
) else (
	echo %device% is connected.
	adb -s %device% shell run-as %package% pwd
	adb -s %device% shell run-as %package% ls -l /data/data/%package%
	adb -s %device% shell run-as %package% ls -l /data/data/%package%/files
::	setlocal enabledelayedexpansion
	for /l %%i in (0,1,9) do adb -s %device% exec-out run-as %package% cat /data/data/%package%/files/tablet.0.%%i.log 2>%device%.0.%%i.errors.txt > %device%.0.%%i.log
	for /l %%i in (0,1,9) do adb -s %device% exec-out run-as %package% cat /data/data/%package%/files/tablet.1.%%i.log 2>%device%.1.%%i.errors.txt > %device%.1.%%i.log
)
