::setlocal enabledelayedexpansion
for %%i in (0a9196e8 0ab62080 0ab63506 0ab62207 0b029b33 0ab61d9b 0b03ae31 015d2109aa080e1a) do (
::for %%i in (0a9196e8) do (
	echo %%i
::	call deletelogfiles.bat %%i
	call getlogfiles.bat %%i
	)
