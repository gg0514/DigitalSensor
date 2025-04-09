using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using SplitView.Views;

namespace SplitView.ViewModels;

public partial class MainViewModel : ViewModelBase
{
    [ObservableProperty]
    private bool isMenuOpen;

    [ObservableProperty]
    private UserControl currentView;

    public MainViewModel()
    {
        CurrentView = new HomeView(); // 초기화면
    }

    [RelayCommand]
    private void ToggleMenu()
    {
        IsMenuOpen = !IsMenuOpen;
    }

    [RelayCommand]
    private void NavigateHome()
    {
        CurrentView = new HomeView();
        IsMenuOpen = true;
    }

    [RelayCommand]
    private void NavigateSettings()
    {
        CurrentView = new SettingView();
        IsMenuOpen = false;
    }
}
