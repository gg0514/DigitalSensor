using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using DigitalSensor.Resources;
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


    // 다국어 지원을 위한 Localize 객체
    public Localize Localize { get; } = new();



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
        Console.WriteLine($"NavigateTo: {pageKey}");

        // Placeholder 
        if (pageKey != "Calib")
        {
            CurrentPage = _navigationService.GetPage(pageKey);
        }
    }
}
