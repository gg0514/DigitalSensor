using Microsoft.Extensions.DependencyInjection;
using NavigationView.ViewModels;
using System;

namespace NavigationView.Services;

public class NavigationService 
{
    private readonly IServiceProvider _provider;
    public NavigationService(IServiceProvider provider) => _provider = provider;

    public object GetPage(string name) => name switch
    {
        "Home" => _provider.GetRequiredService<HomeViewModel>(),
        "Setting" => _provider.GetRequiredService<SettingViewModel>(),
        "Serial" => _provider.GetRequiredService<SettingViewModel>(),
        _ => null
    };
}