using Android.App;
using Android.Content.PM;

using Avalonia;
using Avalonia.Android;
using Avalonia.Controls.ApplicationLifetimes;
using DigitalSensor.USB;
using Java.Util;
using Microsoft.Extensions.DependencyInjection;

namespace DigitalSensor.Android;

[Activity(
    Label = "DigitalSensor.Android",
    Theme = "@style/MyTheme.NoActionBar",
    Icon = "@drawable/bluesen",
    MainLauncher = true,
    ConfigurationChanges = ConfigChanges.Orientation | ConfigChanges.ScreenSize | ConfigChanges.UiMode)]
public class MainActivity : AvaloniaMainActivity<App>
{
    protected override AppBuilder CustomizeAppBuilder(AppBuilder builder)
    {
        App.RegisterPlatformService = OnRegisterPlatformService;

        return base.CustomizeAppBuilder(builder)
            .WithInterFont();
    }

    private void OnRegisterPlatformService(IServiceCollection services)
    {
        //services.AddSingleton<IUsbService, UsbService>();
        //services.AddSingleton<IUsbService, UsbSerial4A>();
        services.AddSingleton<IUsbService, UsbSerialFA>();
    }

    public override void OnBackPressed()
    {
        // Avalonia View에 접근
        if (Avalonia.Application.Current?.ApplicationLifetime is ISingleViewApplicationLifetime lifetime &&
            lifetime.MainView is DigitalSensor.Views.MainView mainView)
        {
            mainView.OnBackRequested();
            // 뒤로 가기 처리
            return;
        }

        // 기본 Back 동작 수행
        base.OnBackPressed();
    }
}
