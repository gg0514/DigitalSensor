using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using NavigationView.Services;
using System;
using System.Collections.ObjectModel;

namespace NavigationView.ViewModels;

public partial class MainViewModel : ViewModelBase
{
    private readonly INavigationService _navigationService;

    [ObservableProperty]
    private object? _currentView;

    [ObservableProperty]
    private int _selectedNavIndex;

    [ObservableProperty]
    private ObservableCollection<NavigationItemViewModel> _navigationItems;


    public MainViewModel()
    {

    }



    public MainViewModel(INavigationService navigationService,
                              HomeViewModel homeViewModel,
                              SettingViewModel settingsViewModel)
    {
        _navigationService = navigationService;
        _navigationService.NavigationChanged += OnNavigationChanged;

        NavigationItems = new ObservableCollection<NavigationItemViewModel>
        {
            new NavigationItemViewModel { Icon = "Home", Label = "Home", ViewModel = homeViewModel },
            new NavigationItemViewModel { Icon = "Settings", Label = "Settings", ViewModel = settingsViewModel }
        };

        // Initialize with home page
        CurrentView = homeViewModel;
    }

    private void OnNavigationChanged(object? sender, object e)
    {
        CurrentView = e;
    }

    [RelayCommand]
    private void NavigateToItem(int index)
    {
        if (index >= 0 && index < NavigationItems.Count)
        {
            CurrentView = NavigationItems[index].ViewModel;
            SelectedNavIndex = index;
        }
    }
}
