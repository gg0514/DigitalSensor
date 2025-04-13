using System;
using System.Collections.Generic;
using NavigationView.ViewModels;
using FluentAvalonia.UI.Controls;

namespace NavigationView.Services;

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
