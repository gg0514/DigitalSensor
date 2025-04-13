using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using NavigationView.Services;
using System.Collections.ObjectModel;
using System.Threading.Tasks;

namespace NavigationView.ViewModels;

public partial class SettingViewModel : ViewModelBase
{
    [ObservableProperty]
    private string _message = "Settings Page";
}