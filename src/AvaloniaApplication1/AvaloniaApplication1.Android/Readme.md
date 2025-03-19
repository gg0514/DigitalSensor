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
    <RuntimeIdentifier>android-arm64</RuntimeIdentifier>    
</PropertyGroup>  




