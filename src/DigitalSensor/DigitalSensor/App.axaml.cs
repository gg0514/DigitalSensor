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

namespace DigitalSensor;


public partial class App : Application
{
    public static Action<IServiceCollection>? RegisterPlatformService;

    private static readonly Lazy<IHost> _globalHost = new Lazy<IHost>(() =>
        Host.CreateDefaultBuilder()
            .ConfigureServices((services) =>
            {
                RegisterPlatformService?.Invoke(services);
                services.AddTransient<NotificationService>();
                services.AddSingleton<MainWindow>();
                services.AddSingleton<MainView>();
                services.AddSingleton<MainViewModel>();
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
            desktop.MainWindow = GlobalHost.Services.GetRequiredService<MainWindow>();
        }
        else if (ApplicationLifetime is ISingleViewApplicationLifetime singleViewPlatform)
        {
            singleViewPlatform.MainView = GlobalHost.Services.GetRequiredService<MainView>();
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