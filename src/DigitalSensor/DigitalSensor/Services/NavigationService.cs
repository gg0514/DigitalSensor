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
            "Calib_Zero" => _provider.GetRequiredService<Calib_ZeroViewModel>(),
            "Calib_1PSample" => _provider.GetRequiredService<Calib_1PSampleViewModel>(),
            "Calib_1PBuffer" => _provider.GetRequiredService<Calib_1PBufferViewModel>(),
            "Calib_2PSample" => _provider.GetRequiredService<Calib_2PSampleViewModel>(),
            "Calib_2PBuffer" => _provider.GetRequiredService<Calib_2PBufferViewModel>(),
            "pH" => _provider.GetRequiredService<TestViewModel>(),
            _ => null
        };
    }
}

public class FakeNavigationService : INavigationService
{
    public FakeNavigationService()
    {
    }

    public object GetPage(string name) => name switch
    {
        "Home" => new HomeViewModel(),
        "Setting" => new SettingViewModel(),
        "Calib_Zero" => new Calib_ZeroViewModel(),
        "Calib_1PSample" => new Calib_1PSampleViewModel(),
        "Calib_1PBuffer" => new Calib_1PBufferViewModel(),
        "Calib_2PSample" => new Calib_2PSampleViewModel(),
        "Calib_2PBuffer" => new Calib_2PBufferViewModel(),
        "pH" => new SettingViewModel(),
        _ => null
    };
}
