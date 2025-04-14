using System;
using System.Collections.Generic;
using DigitalSensor.ViewModels;
using FluentAvalonia.UI.Controls;

namespace DigitalSensor.Services;

public class FakeNavigationService : INavigationService
{
    public FakeNavigationService() 
    {
    }

    public object GetPage(string name) => name switch
    {
        "Home" => new HomeViewModel(),
        "Setting" => new SettingViewModel(),
        "Serial" => new SettingViewModel(),
        _ => null
    };
}
