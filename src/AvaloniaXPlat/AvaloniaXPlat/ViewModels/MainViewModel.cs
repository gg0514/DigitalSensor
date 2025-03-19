using CommunityToolkit.Mvvm.ComponentModel;

namespace AvaloniaXPlat.ViewModels;

public partial class MainViewModel : ViewModelBase
{
    [ObservableProperty]
    private string _greeting = "Welcome to Avalonia!";
}
