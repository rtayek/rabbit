@echo off
::setlocal enabledelayedexpansion
set device=%1
set package=com.tayek.tablet.gui.android.cb7
set destination=log/files
adb devices -l | grep %device% >nul
if errorlevel 1 (
    echo %device% is not connected!
) else (
	echo %device% is connected.
	adb -s %device% shell run-as %package% pwd
	adb -s %device% shell run-as %package% ls -l /data/data/%package%
	adb -s %device% shell run-as %package% ls -l /data/data/%package%/files
	for /l %%j in (0,1,3) do (
		for /l %%i in (0,1,9) do adb -s %device% exec-out run-as %package% cat /data/data/%package%/files/tablet.%%j.%%i.log 2>%destination%/%device%.%%j.%%i.errors.txt > %destination%/%device%.%%j.%%i.log
	)
)
