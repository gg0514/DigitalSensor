@echo off
setlocal

set CONFIG=Debug
set RUNTIME=android-arm64
set FRAMEWORK=net8.0-android

echo [1/2] APK 빌드 및 패키징 중...
dotnet publish -c %CONFIG% -r %RUNTIME% -f %FRAMEWORK% --no-restore
if errorlevel 1 exit /b 1

set APK_PATH=
for %%F in (bin\%CONFIG%\%FRAMEWORK%\%RUNTIME%\publish\*.apk) do set APK_PATH=%%F

if "%APK_PATH%"=="" (
    echo APK 파일을 찾을 수 없습니다.
    exit /b 1
)

echo [2/2] APK 설치 중...
adb install -r "%APK_PATH%"
if errorlevel 1 exit /b 1

echo 완료! 설치된 APK: %APK_PATH%
endlocal
pause