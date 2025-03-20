using System;
using Android.App;
using Android.Content.PM;
using Android.OS;
using Avalonia.Android;
using AvaloniaUsbSerial.Services;
using Microsoft.Extensions.DependencyInjection;


namespace AvaloniaUsbSerial.Android;

[Activity(
    Label = "USB 시리얼 통신",
    Theme = "@style/MyTheme.NoActionBar",
    Icon = "@drawable/icon",
    LaunchMode = LaunchMode.SingleTop,
    ConfigurationChanges = ConfigChanges.Orientation | ConfigChanges.ScreenSize | ConfigChanges.UiMode)]

public class MainActivity : AvaloniaMainActivity<App>
{
    protected override void OnCreate(Bundle savedInstanceState)
    {
        base.OnCreate(savedInstanceState);

        // Android 구현 등록
        var app = Application as App;
        var services = (app.Services as ServiceProvider).CreateScope().ServiceProvider;

        // ISerialService의 Android 구현체 등록
        services.GetRequiredService<ServiceCollection>().AddSingleton<ISerialService, SerialService>();
    }

    // USB 권한 요청 처리를 위한 메소드 (필요한 경우 구현)
}
