using Avalonia;
using Avalonia.Controls;
using Avalonia.Controls.ApplicationLifetimes;
using Avalonia.Data.Core.Plugins;
using Avalonia.Markup.Xaml;

using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;

using DigitalSensor.Services;
using DigitalSensor.ViewModels;
using DigitalSensor.Views;
using System;
using System.Threading;
using DigitalSensor.Extensions;

namespace DigitalSensor;


public partial class App : Application
{
    public static Action<IServiceCollection>? RegisterPlatformService;

    private static readonly Lazy<IHost> _globalHost = new Lazy<IHost>(() =>
        Host.CreateDefaultBuilder()
            .ConfigureServices((services) =>
            {
                // 여기서 UsbSerive의 인터페이스와 구현체를 등록합니다.
                RegisterPlatformService?.Invoke(services);

                services.AddSingleton<INavigationService, NavigationService>();
                services.AddSingleton<IMonitoringService, MonitoringService>();
                services.AddSingleton<ISensorService, SensorService>();
                services.AddTransient<ModbusService>();
                services.AddTransient<NotificationService>();

                services.AddSingleton<MainWindow>();
                services.AddSingleton<MainView>();
                services.AddSingleton<MainViewModel>();
                services.AddSingleton<TestView>();
                services.AddSingleton<TestViewModel>();
                services.AddSingleton<HomeView>();
                services.AddSingleton<HomeViewModel>();
                services.AddSingleton<SettingView>();
                services.AddSingleton<SettingViewModel>();
            })
            .Build());

    public static IHost GlobalHost
    {
        get
        {
            Console.WriteLine("GlobalHost accessed!");
            return _globalHost.Value;
        }
    }

    public override void Initialize()
    {
        AvaloniaXamlLoader.Load(this);
    }

    public override async void OnFrameworkInitializationCompleted()
    {
        if (ApplicationLifetime is IClassicDesktopStyleApplicationLifetime desktop)
        {
            desktop.MainWindow = GlobalHost.GetService<MainWindow>();
        }
        else if (ApplicationLifetime is ISingleViewApplicationLifetime singleViewPlatform)
        {
            singleViewPlatform.MainView = GlobalHost.GetService<MainView>();
        }

        base.OnFrameworkInitializationCompleted();

        try
        {
            await GlobalHost.StartAsync();
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Host Start Failed: {ex.Message}");
        }
    }
}