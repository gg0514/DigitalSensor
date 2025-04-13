using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using NavigationView.Services;
using NavigationView.Views;
using System;
using System.Collections.ObjectModel;

namespace NavigationView.ViewModels;

public partial class MainViewModel : ViewModelBase
{
    private readonly INavigationService _navigationService;

    [ObservableProperty]
    private object? _currentPage;

    public MainViewModel(INavigationService navigationService)
    {
        _navigationService = navigationService;
        NavigateTo("Home");
    }

    public MainViewModel()
    {
        _navigationService = new FakeNavigationService();
        NavigateTo("Home");
    }


    [RelayCommand]
    private void NavigateTo(string pageKey)
    {
        CurrentPage = _navigationService.GetPage(pageKey);
    }
}
