using System;
using Avalonia;
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
        services.AddSingleton<IUsbService, UsbService>();
    }
}
