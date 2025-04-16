using Microsoft.Extensions.DependencyInjection;
using DigitalSensor.ViewModels;
using System;
using System.Diagnostics;
using Avalonia.Interactivity;


namespace DigitalSensor.Services;

public interface INavigationService
{
    object GetPage(string name);
}

public class NavigationService : INavigationService
{
    private readonly IServiceProvider _provider;
    public NavigationService(IServiceProvider provider) => _provider = provider;

    public object GetPage(string name)
    {

        return name switch
        {
            "Home" => _provider.GetRequiredService<HomeViewModel>(),
            "Setting" => _provider.GetRequiredService<SettingViewModel>(),
            "pH" => _provider.GetRequiredService<TestViewModel>(),
            _ => null
        };
    }
}