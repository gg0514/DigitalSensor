## 안드로이드 빌드 및 배포 테스트

* Android 6.0 - API 23 장치의 경우에는 net9.0-android35.0의 TargetFramework를 사용할 수 없음!  
                                               net8.0-android34.0의 TargetFramework에서는 가능함!  
 
* Android 10.0 - API 29 장치의 경우에는 net9.0-android35.0의 TargetFramework를 사용 가능함!  


실제 디바이스 테스트결과, 핵심은 다음과 같다.

    <TargetFramework>net8.0-android34.0</TargetFramework>  
    <SupportedOSPlatformVersion>23</SupportedOSPlatformVersion>  
  
✅ TargetFramework는 프로젝트가 컴파일되는 대상 OS 버전(API Level)을 의미  
✅ SupportedOSPlatformVersion은 실행 가능한 최소 OS 버전(API Level)을 의미  
✅ 두 값이 충돌하지 않도록 설정해야 하며, 낮은 API 버전에서도 실행 가능하도록 코드를 작성하면 범위를 넓힐 수 있음  



## publish 방법  
dotnet publish -c Release -f net8.0-android34.0  


## apk 설치  
adb install com.example.myapp-Signed.apk  


## 기기 ABI 확인  
adb shell getprop ro.product.cpu.abi  


## APK의 ABI 확인  
aapt dump badging   


## RuntimeIdentifier 설정 (64비트)  
<PropertyGroup>  
    <RuntimeIdentifier>android-arm64</RuntimeIdentifier>  
</PropertyGroup>  


## RuntimeIdentifier 설정 (32비트)  
<PropertyGroup>  
    <RuntimeIdentifier>android-arm</RuntimeIdentifier>    
</PropertyGroup>  





