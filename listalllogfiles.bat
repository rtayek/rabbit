@echo off
set package=com.tayek.tablet.gui.android.cb7
:: the files will not show up until the app is run once after an install
::setlocal enabledelayedexpansion
::for %%i in (ab97465ca5e2af1a) do (
::for %%i in (0a9196e8) do (
for %%i in (0a9196e8 0ab62080 0ab63506 0ab62207 0b029b33 0ab61d9b 0b03ae31 015d2109aa080e1a) do (
	adb devices -l | grep %%i >nul
	if errorlevel 1 (
	    echo %%i is not connected!
	) else (
		echo %%i is connected.
		adb -s %%i shell run-as %package% pwd
		adb -s %%i shell run-as %package% ls -l files
	)
)