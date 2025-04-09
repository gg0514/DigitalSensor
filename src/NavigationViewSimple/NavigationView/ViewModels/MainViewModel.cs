using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using NavigationView.Views;
using System;
using System.Collections.ObjectModel;

namespace NavigationView.ViewModels;

public partial class MainViewModel : ViewModelBase
{
    [ObservableProperty]
    private UserControl currentView;

    public MainViewModel()
    {
        CurrentView = new HomeView();
    }

    [RelayCommand]
    private void NavigateHome()
    {
        CurrentView = new HomeView();
    }

    [RelayCommand]
    private void NavigateSettings()
    {
        CurrentView = new SettingView();
    }
}
