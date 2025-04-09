using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using System.Collections.ObjectModel;
using System.Threading.Tasks;

namespace NavigationView.ViewModels;

public partial class SettingViewModel : ViewModelBase
{
    [ObservableProperty]
    private bool _isDarkTheme;

    [ObservableProperty]
    private bool _notificationsEnabled;

    public SettingViewModel()
    {
        _isDarkTheme = false;
        _notificationsEnabled = true;
    }

    [RelayCommand]
    private void SaveSettings()
    {
        // Save settings logic would go here
    }
}