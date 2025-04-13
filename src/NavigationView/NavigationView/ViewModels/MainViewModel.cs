using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using FluentAvalonia.UI.Controls;
using Microsoft.Extensions.DependencyInjection;
using NavigationView.Services;
using System;
using System.Collections.ObjectModel;

namespace NavigationView.ViewModels;

public partial class MainViewModel : ViewModelBase
{
    private readonly INavigationService _navigationService;

    [ObservableProperty]
    private object _currentPage;

    [ObservableProperty]
    private NavigationViewItem _selectedMenuItem;

    public ObservableCollection<NavigationViewItem> MenuItems { get; } = new();

    public MainViewModel(INavigationService navigationService, HomeViewModel homeViewModel)
    {
        _navigationService = navigationService;
        _navigationService.NavigateTo(homeViewModel); // 초기 화면을 홈으로 설정
        CurrentPage = homeViewModel;

        // 메뉴 항목 초기화 (여기서만 정의)
        MenuItems.Add(new NavigationViewItem { Content = "Home", Tag = "Home" });
        MenuItems.Add(new NavigationViewItem { Content = "Settings", Tag = "Settings" });

        SelectedMenuItem = MenuItems[0]; // 기본 선택
        SelectedMenuItemChanged();
    }

    partial void OnSelectedMenuItemChanged(NavigationViewItem oldValue, NavigationViewItem newValue)
    {
        SelectedMenuItemChanged();
    }

    private void SelectedMenuItemChanged()
    {
        switch (SelectedMenuItem?.Tag?.ToString())
        {
            case "Home":
                _navigationService.NavigateTo(ServiceProviderFactory.ServiceProvider.GetRequiredService<HomeViewModel>());
                break;
            case "Settings":
                _navigationService.NavigateTo(ServiceProviderFactory.ServiceProvider.GetRequiredService<SettingViewModel>());
                break;
        }
        CurrentPage = _navigationService.CurrentViewModel;
    }
}

// DI를 위한 간단한 팩토리 (App에서 접근 가능하도록)
public static class ServiceProviderFactory
{
    public static IServiceProvider ServiceProvider { get; set; }
}