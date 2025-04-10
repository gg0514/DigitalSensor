﻿using Avalonia;
using Avalonia.Controls.ApplicationLifetimes;
using Avalonia.Data.Core.Plugins;
using Avalonia.Markup.Xaml;
using Avalonia.Styling;
using FluentAvalonia.Styling;
using Microsoft.Extensions.DependencyInjection;

using NavigationView.ViewModels;
using NavigationView.Views;
using System;
using System.Linq;

namespace NavigationView;

public partial class App : Application
{
    public IServiceProvider? Services { get; private set; }

    public override void Initialize()
    {
        AvaloniaXamlLoader.Load(this);
    }

    public override void OnFrameworkInitializationCompleted()
    {
        // Line below is needed to remove Avalonia data validation.
        // Without this line you will get duplicate validations from both Avalonia and CT
        BindingPlugins.DataValidators.RemoveAt(0);


        //// FluentAvalonia 초기화
        //var fa = new FluentAvaloniaTheme();
        //fa.PreferSystemTheme = false;
        //Styles.Insert(0, fa);


        // Setup DI container
        var serviceCollection = new ServiceCollection();
        ConfigureServices(serviceCollection);
        Services = serviceCollection.BuildServiceProvider();


        if (ApplicationLifetime is IClassicDesktopStyleApplicationLifetime desktop)
        {
            desktop.MainWindow = new MainWindow
            {
                DataContext = Services.GetRequiredService<MainViewModel>()
            };
        }
        else if (ApplicationLifetime is ISingleViewApplicationLifetime singleViewPlatform)
        {
            singleViewPlatform.MainView = new MainView
            {
                DataContext = Services.GetRequiredService<MainViewModel>()
            };
        }

        // Dark 테마 설정
        //Application.Current.RequestedThemeVariant = Avalonia.Styling.ThemeVariant.Dark;

        base.OnFrameworkInitializationCompleted();
    }

    public void ConfigureServices(IServiceCollection services)
    {
        // Register view models
        services.AddTransient<MainViewModel>();
        services.AddTransient<HomeViewModel>();
        services.AddTransient<SettingViewModel>();
    }
}
