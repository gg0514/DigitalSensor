using Microsoft.Extensions.DependencyInjection;
using DigitalSensor.ViewModels;
using DigitalSensor.Services;
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
        IMonitoringService monitoringService = _provider.GetService<IMonitoringService>();

        monitoringService?.SetCurrentPage(name);

        return name switch
        {
            "Home" => _provider.GetRequiredService<HomeViewModel>(),
            "Setting" => _provider.GetRequiredService<SettingViewModel>(),
            "Setting_Modbus" => new SettingModbusViewModel(),
            "Setting_Calib" => new SettingCalibViewModel(),
            "Setting_Serial" => new SettingSerialViewModel(),
            "Calib_Zero" => _provider.GetRequiredService<Calib_ZeroViewModel>(),
            "Calib_1PSample" => _provider.GetRequiredService<Calib_1PSampleViewModel>(),
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
        "Setting_Modbus" => new SettingModbusViewModel(),
        "Setting_Calib" => new SettingCalibViewModel(),
        "Setting_Serial" => new SettingSerialViewModel(),
        "Calib_Zero" => new Calib_ZeroViewModel(),
        "Calib_1PSample" => new Calib_1PSampleViewModel(),
        "Calib_2PBuffer" => new Calib_2PBufferViewModel(),
        "pH" => new SettingViewModel(),
        _ => null
    };
}
