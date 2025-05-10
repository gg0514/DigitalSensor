using System;
using Avalonia;
using DigitalSensor.USB;
using Microsoft.Extensions.DependencyInjection;

namespace DigitalSensor.Desktop;

class Program
{
    // Initialization code. Don't use any Avalonia, third-party APIs or any
    // SynchronizationContext-reliant code before AppMain is called: things aren't initialized
    // yet and stuff might break.
    [STAThread]
    public static void Main(string[] args) => BuildAvaloniaApp()
        .StartWithClassicDesktopLifetime(args);

    // Avalonia configuration, don't remove; also used by visual designer.
    public static AppBuilder BuildAvaloniaApp()
    {
        App.RegisterPlatformService = OnRegisterPlatformService;

        var _ = App.GlobalHost;

        return AppBuilder.Configure<App>()
            .UsePlatformDetect()
            .WithInterFont()
            .LogToTrace();
    }

    private static void OnRegisterPlatformService(IServiceCollection services)
    {
        // Interface와 Implementation을 등록하고,
        // 사용 코드에서는 Interface를 사용하여 작업을 수행한다.
        services.AddSingleton<IUsbService, UsbService>();


        // 다음과 같이 Implementation만을 등록하는 방법도 있다.
        // 사용 코드에서는 Implementation를 사용하여 작업을 수행한다.
        // services.AddSingleton<UsbService>();
    }
}
