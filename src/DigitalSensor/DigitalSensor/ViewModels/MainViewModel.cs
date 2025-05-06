using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using DigitalSensor.Services;
using DigitalSensor.Views;
using System;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.Xml.Linq;

namespace DigitalSensor.ViewModels;

public partial class MainViewModel : ViewModelBase
{
    private readonly INavigationService _navigationService;

    [ObservableProperty]
    private object? _currentPage;

    public MainViewModel(INavigationService navigationService)
    {
        _navigationService = navigationService;
    }

    public MainViewModel()
    {
        _navigationService = new FakeNavigationService();
    }


    [RelayCommand]
    public void NavigateTo(string pageKey)
    {
        Debug.WriteLine($"NavigateTo: {pageKey}");

        // Placeholder 
        if (pageKey != "Calib")
        {
            CurrentPage = _navigationService.GetPage(pageKey);
        }
    }
}
