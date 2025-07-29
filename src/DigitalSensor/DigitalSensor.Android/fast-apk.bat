@echo off
setlocal

set CONFIG=Debug
set RUNTIME=android-arm64
set FRAMEWORK=net8.0-android

echo [1/2] APK ���� �� ��Ű¡ ��...
dotnet publish -c %CONFIG% -r %RUNTIME% -f %FRAMEWORK% --no-restore
if errorlevel 1 exit /b 1

set APK_PATH=
for %%F in (bin\%CONFIG%\%FRAMEWORK%\%RUNTIME%\publish\*.apk) do set APK_PATH=%%F

if "%APK_PATH%"=="" (
    echo APK ������ ã�� �� �����ϴ�.
    exit /b 1
)

echo [2/2] APK ��ġ ��...
adb install -r "%APK_PATH%"
if errorlevel 1 exit /b 1

echo �Ϸ�! ��ġ�� APK: %APK_PATH%
endlocal
pause