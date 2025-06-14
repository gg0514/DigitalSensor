﻿using Avalonia;
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
using DigitalSensor.Models;
using DigitalSensor.Utils;
using DigitalSensor.Resources;
using System.Globalization;

namespace DigitalSensor;


public partial class App : Application
{
    public static Action<IServiceCollection>? RegisterPlatformService;

    private static readonly Lazy<IHost> _globalHost = new Lazy<IHost>(() =>
    {
        var host = Host.CreateDefaultBuilder()
            .ConfigureServices((services) =>
            {
                // 여기서 UsbService의 인터페이스와 구현체를 등록합니다.
                RegisterPlatformService?.Invoke(services);

                services.AddSingleton<INavigationService, NavigationService>();
                services.AddSingleton<IMonitoringService, MonitoringService>();
                services.AddSingleton<ISensorService, SensorService>();
                services.AddSingleton<IModbusService, ModbusService>();
                services.AddSingleton<NotificationService>();

                services.AddSingleton<MainWindow>();
                services.AddSingleton<MainView>();
                services.AddSingleton<MainViewModel>();
                services.AddSingleton<TestView>();
                services.AddSingleton<TestViewModel>();
                services.AddSingleton<HomeView>();
                services.AddSingleton<HomeViewModel>();
                services.AddSingleton<SettingView>();
                services.AddSingleton<SettingViewModel>();
                services.AddSingleton<SettingModbusView>();
                services.AddSingleton<SettingModbusViewModel>();
                services.AddSingleton<SettingCalibView>();
                services.AddSingleton<SettingCalibViewModel>();
                services.AddSingleton<SettingSerialView>();
                services.AddSingleton<SettingSerialViewModel>();
                services.AddSingleton<Calib_ZeroView>();
                services.AddSingleton<Calib_ZeroViewModel>();
                services.AddSingleton<Calib_1PSampleView>();
                services.AddSingleton<Calib_1PSampleViewModel>();
                services.AddSingleton<Calib_2PBufferView>();
                services.AddSingleton<Calib_2PBufferViewModel>();

                // 지연 등록
                services.AddSingleton<AppSettings>(sp =>
                {
                    var jobject = JsonLoader.Load_AppSettings("appsettings.json");
                    return new AppSettings(jobject);
                });

                services.AddSingleton<Lazy<AppSettings>>(sp =>
                    new Lazy<AppSettings>(() => sp.GetRequiredService<AppSettings>()));

            })
            .Build();

        // 즉시 생성할 인스턴스 Resolve
        // LED Tx, Rx Signal 구독 등록 목적
        _ = host.Services.GetRequiredService<Calib_ZeroViewModel>();
        _ = host.Services.GetRequiredService<Calib_1PSampleViewModel>();
        _ = host.Services.GetRequiredService<Calib_2PBufferViewModel>();


        return host;
    });

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

        // 전체스레드에서 동작함
        LocalizationManager.SetCulture("en-US");
        //LocalizationManager.SetCulture("ko-KR");
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