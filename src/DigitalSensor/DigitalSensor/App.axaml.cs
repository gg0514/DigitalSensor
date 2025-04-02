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

namespace DigitalSensor;


public partial class App : Application
{

    public static Action<IServiceCollection>? RegisterPlatformService;

    public static IHost GlobalHost => Host.CreateDefaultBuilder()
        .ConfigureServices((services) =>
        {
            RegisterPlatformService?.Invoke(services);

            services.AddTransient<NotificationService>();
            services.AddSingleton<MainWindow>();
            services.AddSingleton<MainView>();
            services.AddSingleton<MainViewModel>();
        }).Build();


    public override void Initialize()
    {
        AvaloniaXamlLoader.Load(this);
    }

    public override void OnFrameworkInitializationCompleted()
    {
        if (Design.IsDesignMode)
        {
            base.OnFrameworkInitializationCompleted();
            return;
        }

        // Line below is needed to remove Avalonia data validation.
        // Without this line you will get duplicate validations from both Avalonia and CT
        // CommunityToolkit를 사용하기 위해서 Avalonia의 데이터 유효성 검사를 제거하는 코드
        BindingPlugins.DataValidators.RemoveAt(0);


        var mainViewModel = new MainViewModel();

        switch (ApplicationLifetime)
        {
            case IClassicDesktopStyleApplicationLifetime desktop:
                desktop.MainWindow = new MainWindow
                {
                    DataContext = mainViewModel,
                };
                break;
            case ISingleViewApplicationLifetime singleViewPlatform:
                singleViewPlatform.MainView = new MainView
                {
                    DataContext = mainViewModel,
                };
                break;
        }

        base.OnFrameworkInitializationCompleted();
    }
}
